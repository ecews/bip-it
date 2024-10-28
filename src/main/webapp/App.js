
'use client'

import { useState, useRef } from 'react'
import {Plus, Send, User, Paperclip, Menu, Edit2, Check, Bot, Loader} from "lucide-react"

export default function Component() {

    const [message, setMessage] = useState('');
    const [isLoading, setIsLoading] = useState(false);
    const [chatMessages, setChatMessages] = useState([]);
    const [stream, setStream] = useState(null);

    const [messages, setMessages] = useState([
        { role: 'assistant', content: 'Hello! How can I assist you today?' },
    ])
    const [input, setInput] = useState('')
    const [selectedFile, setSelectedFile] = useState(null)
    const [isSidebarOpen, setIsSidebarOpen] = useState(true)
    const fileInputRef = useRef(null)
    const [chatHistory, setChatHistory] = useState([
        { id: 1, title: 'Previous chat 1' },
        { id: 2, title: 'Previous chat 2' },
    ])
    const [editingChatId, setEditingChatId] = useState(null)
    const [editedTitle, setEditedTitle] = useState('')

    const handleSubmit = async (e) => {

        if (!input) return;
        const newMessage = { role: 'user', content: input, file: selectedFile }
        setMessages([...messages, newMessage])
        setInput('')
        setIsLoading(true)
        try {
            // Initiate the fetch request to the SSE endpoint
            const response = await fetch(`/api/v1/ai/chat/stream?message=${encodeURIComponent(input)}`, {
                method: 'GET',
                headers: {
                    'Accept': 'text/event-stream'
                }
            });

            if (!response.body) {
                console.error("No response body found.");
                return;
            }
            // Use the stream reader for processing
            const reader = response.body.getReader();
            await processStream(reader);
        } catch (error) {
            console.error('Error fetching chatbot response:', error);
            const newMessage = { role: 'user', content: 'System\', \'Oops! Something went wrong while fetching the response. Please try again in a moment. Thanks for your patience! 😊',
                file: selectedFile }
            setMessages([...messages, newMessage])
        } finally {
            setIsLoading(false);
        }

    };

    // Function to process each chunk in the stream
    const processStream = async (reader) => {
        const decoder = new TextDecoder('utf-8');
        let accumulatedContent = ""; // Stores the complete message being formed

        try {
            while (true) {
                const { done, value } = await reader.read();
                if (done) break;

                const chunk = decoder.decode(value, { stream: true }).trim();
                const cleanChunk = chunk.replace(/data:\s?/g, '');

                // Split chunk into sentences
                const sentences = cleanChunk.replace(/\./g, '.\n').split('\n');

                for (const sentence of sentences) {
                    if (sentence) {
                        accumulatedContent += sentence + " ";
                        setMessages((prevMessages) => {
                            const lastMessage = prevMessages[prevMessages.length - 1];
                            if (lastMessage.role === 'assistant') {
                                // Update last assistant message
                                return [...prevMessages.slice(0, -1), { ...lastMessage, content: accumulatedContent }];
                            } else {
                                // Add new assistant message if the last message isn't from assistant
                                return [...prevMessages, { role: 'assistant', content: accumulatedContent }];
                            }
                        });
                        await new Promise((resolve) => setTimeout(resolve, 100));
                    }
                }
            }
        } catch (error) {
            console.error('Error processing stream:', error);
            setMessages((prevMessages) => [
                ...prevMessages,
                { role: 'assistant', content: '[Error: Stream interrupted. Please try again.]' }
            ]);
        }
    };

    const handleFileChange = (event) => {
        const file = event.target.files[0]
        if (file) {
            setSelectedFile(file)
        }
    }

    const triggerFileInput = () => {
        fileInputRef.current.click()
    }

    const toggleSidebar = () => {
        setIsSidebarOpen(!isSidebarOpen)
    }

    const startEditing = (id, title) => {
        setEditingChatId(id)
        setEditedTitle(title)
    }

    const saveEditedTitle = (id) => {
        setChatHistory(chatHistory.map(chat =>
            chat.id === id ? { ...chat, title: editedTitle } : chat
        ))
        setEditingChatId(null)
    }

    const addNewChat = () => {
        const newChat = { id: chatHistory.length + 1, title: `New chat ${chatHistory.length + 1}` }
        setChatHistory([...chatHistory, newChat])
    }

    const handleKeyDown = (e) => {
        if (e.key === "Enter" && !e.shiftKey) {
            e.preventDefault();  // Prevent newline in the textarea
            handleSubmit();      // Call handleSubmit function
        }
    };

    return (
        <div className="flex h-screen bg-white">
            {/* Sidebar */}
            {/*<div className={`bg-gray-100 border-r border-gray-200 transition-all duration-300 ${isSidebarOpen ? 'w-64' : 'w-0'} overflow-hidden`}>
                <div className="p-4 border-b border-gray-200">
                    <button onClick={addNewChat} className="flex items-center px-3 py-2 text-black bg-white border border-black rounded-lg hover:bg-blue-50 w-full">
                        <Plus className="w-5 h-5 mr-1" /> New Chat
                    </button>
                </div>
                <div className="p-4 space-y-2">
                    {chatHistory.map(chat => (
                        <div key={chat.id} className="flex items-center space-x-2 group">
                            {editingChatId === chat.id ? (
                                <>
                                    <input
                                        type="text"
                                        value={editedTitle}
                                        onChange={(e) => setEditedTitle(e.target.value)}
                                        className="flex-1 px-2 py-1 border rounded-md"
                                    />
                                    <button onClick={() => saveEditedTitle(chat.id)} className="text-green-500">
                                        <Check className="w-5 h-5" />
                                    </button>
                                </>
                            ) : (
                                <>
                                    <span className="flex-1">{chat.title}</span>
                                    <button onClick={() => startEditing(chat.id, chat.title)} className="text-blue-500 hidden group-hover:block">
                                        <Edit2 className="w-4 h-4" />
                                    </button>
                                </>
                            )}
                        </div>
                    ))}
                </div>
            </div>*/}

            {/* Main Chat Area */}
            <div className="flex-1 flex flex-col">
                <header className="flex items-center justify-between bg-white p-4">
                    <div></div>
                    {/*<button onClick={toggleSidebar}>
                        <Menu className="h-6 w-10 text-black-500"/>
                    </button>*/}
                    <h1 className="text-xl font-semibold text-gray-700">
                        <h1>Welcome to Bip.it</h1>
                    </h1>
                    <div className="w-6 h-6"/>
                    {/* Placeholder for alignment */}
                </header>
                <hr className="my-3"/>
                <div className="flex-1 overflow-y-auto p-4 bg-white flex flex-col items-center">
                    <div className="w-full max-w-2xl space-y-4 mt-6 mb-6">
                        {messages.map((message, index) => (
                            <div
                                key={index}
                                className={`flex ${message.role === 'user' ? 'justify-end' : 'justify-start'} items-start space-x-2`}
                            >
                                {/* Render icon and message content conditionally */}
                                {message.role === 'user' ? (
                                    <>
                                        <div
                                            className="px-4 py-2 rounded-lg max-w-[70%] bg-gray-200 text-gray-800 self-end text-justify">
                                            {message.content}
                                        </div>
                                        <div className="flex items-center">
                                            <User className="h-6 w-6 text-gray-500"/>
                                        </div>
                                    </>
                                ) : (
                                    <>
                                        <div className="flex items-center">
                                            <Bot className="h-6 w-6 text-gray-500"/>
                                        </div>
                                        <div
                                            dangerouslySetInnerHTML={{ __html: message.content }}
                                            className="px-4 py-2 rounded-lg w-full text-gray-800 self-start text-justify markdown-content">
                                        </div>
                                    </>
                                )}
                            </div>
                        ))}
                    </div>
                </div>

                <footer
                    className="flex items-center p-4 bg-gray-200 shadow-md w-full max-w-2xl mx-auto rounded-3xl mb-4">
                    <button onClick={triggerFileInput} className="mr-2">
                        <Paperclip className="h-6 w-6 text-gray-500"/>
                    </button>
                    <input
                        type="file"
                        ref={fileInputRef}
                        onChange={handleFileChange}
                        style={{display: 'none'}}
                    />
                    <textarea
                        value={input}
                        onChange={(e) => setInput(e.target.value)}
                        onKeyDown={handleKeyDown}  // Handle Enter key press
                        className="flex-1 border rounded-md bg-gray-200 px-4 py-2 outline-none resize-y"
                        placeholder="Type a message..."
                        rows={1}
                        style={{
                            minHeight: "40px",  // Minimum height (2 rows approx)
                            maxHeight: "120px", // Maximum height (e.g., 6 rows approx)
                            overflowY: "auto"   // Adds scrollbar if content exceeds max height
                        }}
                    />
                    <button onClick={handleSubmit}
                            className="ml-2 p-2 bg-black text-white rounded-lg hover:bg-auto">
                        {isLoading ? (
                            <Loader className="h-5 w-5 animate-spin"/>  // Display wait icon with animation
                        ) : (
                            <Send className="h-5 w-5"/>
                        )}
                    </button>
                </footer>
                <p><b>Powered by ECEWS</b></p>
            </div>
        </div>
    )
}
