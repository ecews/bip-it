// import React, { useState } from 'react';
// import './App.css';
//
// function App() {
//     const [message, setMessage] = useState('');
//     const [isLoading, setIsLoading] = useState(false);
//
//     // Handle form submit
//     const handleSubmit = async (e) => {
//         e.preventDefault();
//         if (!message) return;
//
//         // Add the user message to chat
//         addMessageToChat('User', message);
//
//         // Clear the input field
//         setMessage('');
//
//         try {
//             setIsLoading(true); // Show loading spinner or indicator
//
//             // Fetch the streaming response
//             const response = await fetchStreamWithRetry(`/api/v1/ai/chat/stream1?message=${encodeURIComponent(message)}`);
//             const reader = response.body.getReader();
//             await processStream(reader);
//         } catch (error) {
//             console.error('Error fetching chatbot response:', error);
//             addMessageToChat('System', 'An error occurred while fetching the response. Please try again.');
//         } finally {
//             setIsLoading(false);
//         }
//     };
//
//     // Fetch stream with retry logic
//     const fetchStreamWithRetry = async (url, retries = 3) => {
//         for (let i = 0; i < retries; i++) {
//             try {
//                 const response = await fetch(url);
//                 if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
//                 return response;
//             } catch (e) {
//                 console.error(`Attempt ${i + 1} failed: ${e.message}`);
//                 if (i === retries - 1) throw e;
//                 await new Promise((resolve) => setTimeout(resolve, 1000)); // Wait 1 second before retrying
//             }
//         }
//     };
//
//     // Process stream and append to chat
//     const processStream = async (reader) => {
//         const decoder = new TextDecoder('utf-8');
//         let accumulatedResponse = '';
//         let botMessageElement = addMessageToChat('Bip.it', ''); // Add empty bot message
//         let contentElement = botMessageElement.querySelector('.message-content');
//
//         try {
//             while (true) {
//                 const { done, value } = await reader.read();
//                 if (done) break;
//
//                 // Decode the incoming chunk
//                 const chunk = decoder.decode(value, { stream: true }).trim();
//                 const cleanChunk = chunk.replace(/data:\s?/g, ''); // Remove 'data: ' prefixes
//
//                 accumulatedResponse += cleanChunk;
//
//                 // Split by periods or other sentence delimiters to simulate incremental response
//                 const sentences = accumulatedResponse.split(/[.?!]\s/);
//
//                 // Only keep unfinished sentences in the accumulator
//                 accumulatedResponse = sentences.pop();
//
//                 // Append sentences one by one to simulate real-time response
//                 sentences.forEach((sentence) => {
//                     contentElement.innerHTML += sentence + '. '; // Append sentence
//                 });
//
//                 // Scroll to the bottom of the chat window to show new messages
//                 scrollToBottom();
//             }
//
//             // Handle any leftover incomplete sentence
//             if (accumulatedResponse) {
//                 contentElement.innerHTML += accumulatedResponse;
//             }
//         } catch (error) {
//             console.error('Error processing stream:', error);
//             contentElement.innerHTML += '<br>[Error: Stream interrupted. Please try again.]';
//         }
//     };
//
//     // Add a message to the chat UI
//     const addMessageToChat = (sender, content) => {
//         const messageElement = document.createElement('div');
//         messageElement.className = `${sender.toLowerCase()}-message ${
//             sender === 'User' ? 'bg-blue-100' : 'bg-gray-100'
//         } p-3 rounded-lg`;
//         messageElement.innerHTML = `
//             <div class="font-bold ${sender === 'User' ? 'text-blue-600' : 'text-green-600'}">${sender}:</div>
//             <div class="message-content">${content}</div>
//         `;
//         document.getElementById('chatMessages').appendChild(messageElement); // Assuming your chat has an ID
//         scrollToBottom();
//         return messageElement;
//     };
//
//     // Scroll to the bottom of the chat window
//     const scrollToBottom = () => {
//         const chatMessagesElement = document.getElementById('chatMessages');
//         chatMessagesElement.scrollTop = chatMessagesElement.scrollHeight;
//     };
//
//     return (
//         <div className="App">
//             <div id="chatMessages" className="chat-messages-container">
//                 {/* Chat messages will be appended here */}
//             </div>
//             <form onSubmit={handleSubmit} className="chat-form">
//                 <input
//                     type="text"
//                     value={message}
//                     onChange={(e) => setMessage(e.target.value)}
//                     placeholder="Type your message..."
//                     className="message-input"
//                 />
//                 <button type="submit" disabled={isLoading}>
//                     {isLoading ? 'Loading...' : 'Send'}
//                 </button>
//             </form>
//         </div>
//     );
// }
//
// export default App;

import React, { useState, useEffect } from 'react';
import './App.css';

function App() {
    const [message, setMessage] = useState('');
    const [isLoading, setIsLoading] = useState(false);
    const [chatMessages, setChatMessages] = useState([]);
    const [stream, setStream] = useState(null);

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!message) return;
        addMessageToChat('User', message);
        setMessage('');
        setIsLoading(true);
        try {
            const response = await fetchStreamWithRetry(`/api/v1/ai/chat/stream?message=${encodeURIComponent(message)}`);
            const reader = response.body.getReader();
            setStream({ reader, message });
            await processStream(reader, message);
        } catch (error) {
            console.error('Error fetching chatbot response:', error);
            addMessageToChat('System', 'Oops! Something went wrong while fetching the response. Please try again in a moment. Thanks for your patience! 😊');
        } finally {
            setIsLoading(false);
        }
    };

    const handleInputChange = async (e) => {
        setMessage(e.target.value);
        if (e.target.value.trim().endsWith('?')) {
            handleSubmit(e);
        }
        // thanks
    };



    const fetchStreamWithRetry = async (url, retries = 3) => {
        for (let i = 0; i < retries; i++) {
            try {
                const response = await fetch(url);
                if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
                return response;
            } catch (e) {
                console.error(`Attempt ${i + 1} failed: ${e.message}`);
                if (i === retries - 1) throw e;
                await new Promise((resolve) => setTimeout(resolve, 1000)); // Wait 1 second before retrying
            }
        }
    };


    const processStream = async (reader, message) => {
        const decoder = new TextDecoder('utf-8');
        let botMessageElement = addMessageToChat('Bip.it', '');
        let contentElement = botMessageElement.querySelector('.message-content');

        try {
            while (true) {
                const { done, value } = await reader.read();
                if (done) break;

                const chunk = decoder.decode(value, { stream: true }).trim();
                const cleanChunk = chunk.replace(/data:\s?/g, '');




                // Split chunk into sentences
                const sentences = cleanChunk.replace(/\./g, '.\n').split('\n');

                // Process each sentence separately
                for (const sentence of sentences) {
                    if (sentence) {
                        contentElement.innerHTML += sentence + '\n\n';
                        scrollToBottom();
                        await new Promise(resolve => setTimeout(resolve, 100));
                    }
                }
            }
        } catch (error) {
            console.error('Error processing stream:', error);
            contentElement.innerHTML += '<br>[Error: Stream interrupted. Please try again.]';
        }
    };




    const addMessageToChat = (sender, content) => {
        const messageElement = document.createElement('div');
        messageElement.className = `${sender.toLowerCase()}-message ${sender === 'User' ? 'bg-blue-100' : 'bg-gray-100'} p-3 rounded-lg`;
        messageElement.innerHTML = `
      <div class="font-bold ${sender === 'User' ? 'text-blue-600' : 'text-green-600'}">${sender}:</div>
      <div class="message-content">${content}</div>
    `;
        document.getElementById('chatMessages').appendChild(messageElement);
        setChatMessages((prevMessages) => [...prevMessages, messageElement]);
        scrollToBottom();
        return messageElement;
    };

    const scrollToBottom = () => {
        const chatMessagesElement = document.getElementById('chatMessages');
        chatMessagesElement.scrollTop = chatMessagesElement.scrollHeight;
    };

    useEffect(() => {
        scrollToBottom();
    }, [chatMessages]);

    return (
        <div className="App">
            <header className="chat-header">
                <h1>Welcome to Bip.it</h1>
                <p>Powered by ECEWS</p>
            </header>
            <div id="chatMessages" className="chat-messages-container">
                <div className="welcome-message">
                    <p>Hello! I'm Bip.it, your AI assistant.</p>
                    <p>How can I help you today?</p>
                </div>
                {/* Chat messages will be appended here */}
            </div>
            <form onSubmit={handleSubmit} className="chat-form">
                <input
                    type="text"
                    value={message}
                    onChange={handleInputChange}
                    placeholder="Type your message..."
                    className="message-input"
                />
                <button type="submit" disabled={isLoading}>
                    {isLoading ? 'Loading...' : 'Send'}
                </button>
            </form>
        </div>
    );
}

export default App;

