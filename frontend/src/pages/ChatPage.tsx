import { Box, Container, VStack, Flex } from '@chakra-ui/react'
import { useChatMessages } from '../features/chat/hooks/useChatMessages'
import { MessageBubble } from '../features/chat/components/MessageBubble'
import { ChatInputArea } from '../features/chat/components/ChatInputArea'
import { useEffect, useRef } from 'react'

const ChatPage = () => {
  const { messages, isConnected, isProcessingMessage, sendMessage } = useChatMessages()
  const messagesEndRef = useRef<HTMLDivElement>(null)

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }

  useEffect(() => {
    scrollToBottom()
  }, [messages])

  return (
    <Box h="100vh" bg="#f2ece4" position="relative">
      <Container maxW="full" h="100%" p={0} display="flex" flexDirection="column">

        <Flex
          flex="1"
          direction="column"
          p={4}
          overflowY="auto"
          css={{
            '&::-webkit-scrollbar': { width: '4px' },
            '&::-webkit-scrollbar-track': { background: 'transparent' },
            '&::-webkit-scrollbar-thumb': { background: '#cbd5e0', borderRadius: '24px' },
          }}
        >
          <VStack align="stretch" gap={4}>
            {messages.map((msg, index) => (
              <MessageBubble key={index} message={msg} />
            ))}
            {isProcessingMessage && (
              <MessageBubble
                message={{ isUser: false, content: '' }}
                isLoading={true}
              />
            )}
            <div ref={messagesEndRef} />
          </VStack>
        </Flex>

        <Box position="relative" h="120px">
          <ChatInputArea
            onSendMessage={sendMessage}
            isWebsocketConnected={isConnected}
          />
        </Box>
      </Container>
    </Box>
  )
}

export default ChatPage
