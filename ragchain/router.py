from typing import Annotated
from fastapi import APIRouter, Depends
from fastapi.responses import StreamingResponse, JSONResponse
from ragchain.dependencies import get_chat_service
from ragchain.service import ChatService

router = APIRouter()

@router.post("/chat/{sessionId}")
async def chat_streaming(
    sessionId: str,
    user_input: str,
    service: Annotated[ChatService, Depends(get_chat_service)]
):
    async def build_json_response():
        async for item in service.astream_chat(session_id=sessionId, user_input=user_input):
            if 'answer' in item:
                yield JSONResponse(content={
                    'type': 'chunk',
                    'content': item['answer']
                    }).body + b'\n'
        yield JSONResponse(content={
            'type': 'end',
            'content': ''
        }).body + b'\n'
    return StreamingResponse(
        build_json_response(), 
        media_type="text/event-stream"
    )
