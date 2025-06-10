import {useEffect, useState, useMemo} from "react";
import {Link, useParams} from "react-router-dom";
import {API_BASE, getBasicAuthHeader} from "./config";
import logo from "./assets/logo.png";
import Message from "./components/Message";
import {useThread} from "./context/ThreadContext";
import InputSection from "./components/InputSection";

function App() {
    const {threadId: threadIdFromParams} = useParams();
    const {threadId, assistantId, setThreadFromId} = useThread();
    const [messages, setMessages] = useState([]);
    const [inputValue, setInputValue] = useState("");
    const [scrollDown, setScrollDown] = useState(true);

    const auth = useMemo(() => getBasicAuthHeader(), []);

    useEffect(() => {
        if (threadIdFromParams && threadIdFromParams !== threadId) {
            setThreadFromId(threadIdFromParams);
        }
    }, [threadIdFromParams, threadId, setThreadFromId]);

    useEffect(() => {
        if (!threadId) return;

        const fetchMessages = () => {
            fetch(`${API_BASE}/api/messages/${threadId}`, {
                method: "GET",
                headers: {
                    Authorization: auth,
                    "Content-Type": "application/json",
                },
                credentials: "include",
            })
                .then((res) => res.json())
                .then((data) => {
                    setMessages(data.messagesList || []);
                })
                .catch(console.error);
        };

        fetchMessages();
        const interval = setInterval(fetchMessages, 2000);

        return () => clearInterval(interval);
    }, [threadId, auth]);

    const handleInputChange = (event) => {
        setInputValue(event.target.value);
    };

    const handleSend = async () => {
        if (!inputValue || !assistantId || !threadId) return;

        try {
            fetch(`${API_BASE}/api/prompt/thread`, {
                method: "POST",
                headers: {
                    Authorization: auth,
                    "Content-Type": "application/json",
                },
                credentials: "include",
                body: JSON.stringify({
                    assistantId,
                    threadId,
                    prompt: inputValue,
                }),
            });

            setInputValue("");
        } catch (error) {
            console.error("Error sending message:", error);
        }
    };

    // useEffect(() => {
    //   const container = document.querySelector(".input-section");
    //   container.scrollTop = container.scrollHeight;
    // }, [messages]);

    const toggleScroll = () => {
        if (scrollDown) {
            window.scrollTo({top: document.body.scrollHeight, behavior: "smooth"});
        } else {
            window.scrollTo({top: 0, behavior: "smooth"});
        }
        setScrollDown(!scrollDown);
    };

    // useEffect(() => {
    //   if (scrollDown) {
    //     window.scrollTo({ top: document.body.scrollHeight, behavior: "smooth" });
    //   }
    // }, [messages, scrollDown]);

    return (
        <div className="chat-container">
            <div className="logo-container">
                <Link to={`/`}>
                    <img className="logo" alt="logo" src={logo}/>
                </Link>
            </div>

            <div className="messages">
                {messages.map((message, index) => (
                    <Message key={index} messages={message}/>
                ))}
            </div>

            <div className="input-wrapper">
                <div className="scroll-toggle">
                    <button
                        onClick={toggleScroll}
                        className={`scroll-button ${
                            scrollDown ? "scroll-up" : "scroll-down"
                        }`}
                    ></button>
                </div>
                <InputSection
                    inputValue={inputValue}
                    onInputChange={handleInputChange}
                    onSend={handleSend}
                    onKeyDown={(e) => {
                        if (e.key === "Enter" && !e.shiftKey) {
                            e.preventDefault();
                            handleSend();
                        }
                    }}
                />
            </div>
        </div>
    );
}

export default App;
