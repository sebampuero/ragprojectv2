import { Box, Flex, Text, Button } from "@chakra-ui/react";
import { useNavigate } from "react-router-dom";

export const PromotedModal = ({
    isOpen = true,
    onClose,
}: {
    isOpen?: boolean;
    onClose?: () => void;
}) => {
    const navigate = useNavigate();

    if (!isOpen) return null;

    return (
        <Flex
            position="fixed"
            inset={0}
            backgroundColor="rgba(0, 0, 0, 0.6)"
            zIndex={1400}
            alignItems="center"
            justifyContent="center"
            onClick={onClose}
        >
            <Box
                backgroundColor="white"
                padding="2.5rem"
                borderRadius="1rem"
                boxShadow="0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05)"
                maxWidth="500px"
                width="90%"
                onClick={(e) => e.stopPropagation()}
            >
                <Flex direction="column" gap="1.5rem" alignItems="center" textAlign="center">
                    <Text color="gray.600" fontSize="md" lineHeight="1.6">
                        This RAG chat app works by answering questions about me. It uses Retrieval Augmented Generation
                        to fetch relevant information from my resume and other personal information and then uses a LLM to generate a response.
                        You can start chatting with me now, click on the button below.
                    </Text>

                    <Button
                        backgroundColor="#2563eb"
                        color="white"
                        paddingX="2rem"
                        paddingY="1rem"
                        borderRadius="0.5rem"
                        fontSize="lg"
                        fontWeight="semibold"
                        transition="background-color 0.2s"
                        _hover={{ backgroundColor: "#1d4ed8" }}
                        onClick={() => navigate('/chat')}
                    >
                        Go to Chat
                    </Button>
                </Flex>
            </Box>
        </Flex>
    );
};

export default PromotedModal;
