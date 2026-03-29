import logging
from ragchain.core.config import settings
from langchain_community.chat_message_histories import RedisChatMessageHistory
from langchain_core.prompts import ChatPromptTemplate
from langchain_core.prompts.chat import MessagesPlaceholder
from langchain_classic.chains.history_aware_retriever import create_history_aware_retriever
from langchain_classic.chains.combine_documents import create_stuff_documents_chain
from langchain_classic.chains.retrieval import create_retrieval_chain
from langchain_deepseek import ChatDeepSeek
from langchain_core.runnables.history import RunnableWithMessageHistory
from ragchain.services.ingestion import ingest
from ragchain.core.vector_store import get_vector_store
from ragchain.core.document_loader import get_document_loader
from typing import AsyncIterator, Any

logger = logging.getLogger(__name__)

contextualize_q_system_prompt = (
    """
    Given a chat history and the latest user question "
    "which might reference context in the chat history, "
    "formulate a standalone question which can be understood "
    "without the chat history. Do NOT answer the question, "
    "just reformulate it if needed and otherwise return it as is."
    """
)

system_prompt = (
    """
    You are Sebastian answering questions about yourself with the given context. 
    If the answer can't be answered with the given context, say you don't know. 
    Use three sentences maximum and keep answers concise. 
    Answer as if you were Sebastian using "I", not third person. 
    Answer in german, english or spanish.
    \n\n
    Context: {context}
    """
)

class ChatService:
    _instance = None

    def __new__(cls, *args, **kwargs):
        if cls._instance is None:
            cls._instance = super().__new__(cls)
            cls._instance._initialized = False
            logger.info("ChatService instance created")
        return cls._instance

    def __init__(self):
        if getattr(self, "_initialized", False):
            return
        self.chain = self._build_chain()
        logger.info("ChatService chain built")
        self._initialized = True

    def _build_chain(self):
        def get_session_history(session_id: str) -> RedisChatMessageHistory:
            return RedisChatMessageHistory(session_id, url=settings.REDIS_URL)

        loader = get_document_loader()
        vector_store = get_vector_store()
        ingest(vector_store, loader)
        retriever = vector_store.as_retriever(
            search_type="similarity", search_kwargs={"k": 3}
        )
        logger.debug(f"Using retriever with config specs: {retriever.config_specs}")
        llm = ChatDeepSeek(
            model="deepseek-chat",
            api_key=settings.DEEPSEEK_API_KEY,
        )
        logger.debug(f"Using LLM: {llm}")
        contextualize_q_prompt = ChatPromptTemplate.from_messages(
            [
                ("system", contextualize_q_system_prompt),
                MessagesPlaceholder("chat_history"),
                ("human", "{input}"),
            ]
        )
        history_aware_retriever = create_history_aware_retriever(
            llm, retriever, contextualize_q_prompt
        )
        qa_prompt = ChatPromptTemplate.from_messages(
            [
                ("system", system_prompt),
                MessagesPlaceholder("chat_history"),
                ("human", "{input}"),
            ]
        )
        question_answer_chain = create_stuff_documents_chain(llm, qa_prompt)
        rag_chain = create_retrieval_chain(history_aware_retriever, question_answer_chain)
        self.conversational_rag_chain = RunnableWithMessageHistory(
            rag_chain,
            get_session_history,
            input_messages_key="input",
            history_messages_key="chat_history",
            output_messages_key="answer",
        )
        logger.debug(f"Using conversational RAG chain with specs: {self.conversational_rag_chain.config_specs}")

    async def astream_chat(self, session_id: str, user_input: str) -> AsyncIterator[Any]:
        logger.debug(f"Using session ID: {session_id} with input: {user_input}")
        return self.conversational_rag_chain.astream({"input": user_input},
                    config={
                        "configurable": {"session_id": session_id}
                    })
