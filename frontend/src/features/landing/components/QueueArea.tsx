import { Text, VStack, Box, Container } from '@chakra-ui/react';
import { useQueueEvents } from '../hooks/useQueueEvents';
import { PromotedModal } from './PromotedModal';

export const QueueArea = () => {
    const { queueSize, isPromotedModalOpen } = useQueueEvents();

    return (
        <Box
            minH="100vh"
            display="flex"
            alignItems="center"
            justifyContent="center"
            bgGradient="to-br"
            p={4}
        >
            <Container
                maxW="lg"
                bg="rgba(255, 255, 255, 0.05)"
                backdropFilter="blur(10px)"
                borderRadius="2xl"
                p={10}
                boxShadow="2xl"
                border="1px solid rgba(255, 255, 255, 0.1)"
            >
                <VStack textAlign="center">
                    <Box
                        p={6}
                        bg="whiteAlpha.100"
                        border="1px solid whiteAlpha.200"
                        w="full"
                        transition="transform 0.2s"
                        _hover={{ transform: 'scale(1.02)' }}
                    >
                        <VStack gap={2}>
                            <Text fontSize="sm" color="whiteAlpha.600" textTransform="uppercase" letterSpacing="widest">
                                You are in the queue position:
                            </Text>
                            <Text fontSize="4xl" fontWeight="bold" color="white">
                                {queueSize}
                            </Text>
                            <Text fontSize="md" color="whiteAlpha.700">
                                Please wait! LLMs can become expensive, only one user at a time can chat with
                                the RAG system.
                            </Text>
                        </VStack>
                    </Box>
                </VStack>
            </Container>
            <PromotedModal isOpen={isPromotedModalOpen} />
        </Box>
    );
};
