import {
    Box,
    Textarea,
    IconButton,
    HStack,
} from "@chakra-ui/react";
import { useState } from "react";
import { LuSend } from "react-icons/lu";

interface ChatInputAreaProps {
    onSendMessage: (content: string) => void;
    isWebsocketConnected: boolean;
}

export const ChatInputArea = ({
    onSendMessage,
    isWebsocketConnected
}: ChatInputAreaProps) => {

    const [inputValue, setInputValue] = useState("");

    const handleSend = () => {
        if (inputValue.trim() === "") return;
        if (!isWebsocketConnected) {
            console.log("Websocket is not connected");
            alert("There is not connection to the server right now, try refreshing the page.");
            return;
        };
        onSendMessage(inputValue);
        setInputValue("");
    };

    const handleKeyDown = (e: React.KeyboardEvent) => {
        if (e.key === "Enter" && !e.shiftKey) {
            e.preventDefault();
            handleSend();
        }
    };

    return (
        <Box p={4} position="absolute" bottom="0" width="full" bg="#f2ece4">

            <HStack gap={2} bg="white" p={1} borderRadius="md" boxShadow="sm">
                <Textarea
                    placeholder="Type a message..."
                    variant="subtle"
                    px={3}
                    py={2}
                    border="none"
                    _focus={{ boxShadow: "none" }}
                    flex={1}
                    value={inputValue}
                    onChange={(e) => setInputValue(e.target.value)}
                    onKeyDown={handleKeyDown}
                />
                <IconButton
                    aria-label="Send message"
                    bg="green.500"
                    color="white"
                    size="sm"
                    _hover={{ bg: "green.600" }}
                    onClick={handleSend}
                >
                    <LuSend />
                </IconButton>
            </HStack>
        </Box>
    );
};