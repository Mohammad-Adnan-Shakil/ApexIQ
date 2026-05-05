import React, { useState, useEffect, useRef } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { MessageCircle, X, Send, Bot } from "lucide-react";

const TelemetryChatbot = () => {
  const [isOpen, setIsOpen] = useState(false);
  const [messages, setMessages] = useState([]);
  const [inputValue, setInputValue] = useState("");
  const [isTyping, setIsTyping] = useState(false);
  const messagesEndRef = useRef(null);

  useEffect(() => {
    if (isOpen && messages.length === 0) {
      // Add welcome message when chat opens for the first time
      const welcomeMessage = {
        id: Date.now(),
        sender: "bot",
        text: "🏁 Welcome to DeltaBox Telemetry Assistant! I can help you analyze driver performance, compare telemetry data, and provide insights about racing metrics. What would you like to know?",
        timestamp: new Date()
      };
      setMessages([welcomeMessage]);
    }
  }, [isOpen, messages.length]);

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  };

  const handleSendMessage = async () => {
    if (!inputValue.trim()) return;

    const userMessage = {
      id: Date.now(),
      sender: "user",
      text: inputValue.trim(),
      timestamp: new Date()
    };

    setMessages(prev => [...prev, userMessage]);
    setInputValue("");
    setIsTyping(true);

    // Simulate bot response (mock API integration)
    setTimeout(() => {
      const botResponse = generateBotResponse(userMessage.text);
      setMessages(prev => [...prev, botResponse]);
      setIsTyping(false);
    }, 1000 + Math.random() * 1000);
  };

  const generateBotResponse = (userInput) => {
    const responses = [
      "Based on the telemetry data, I can see that driver performance varies significantly through different corners of the circuit.",
      "The throttle application patterns suggest this driver is more aggressive on corner exit, which could explain the faster lap times.",
      "Braking points appear consistent, but there's room for improvement in the final sector.",
      "The speed trace shows good momentum through the high-speed sections, which is crucial for this circuit.",
      "Gear selection looks optimal for the chosen racing line. The driver is maximizing the power band effectively.",
      "The telemetry indicates slight understeer in mid-corner, which could be addressed with setup adjustments."
    ];

    const randomResponse = responses[Math.floor(Math.random() * responses.length)];
    
    return {
      id: Date.now() + 1,
      sender: "bot",
      text: randomResponse,
      timestamp: new Date()
    };
  };

  const handleKeyPress = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSendMessage();
    }
  };

  return (
    <div className="fixed bottom-4 right-4 z-50">
      {/* Chat Button */}
      <AnimatePresence>
        {!isOpen && (
          <motion.button
            initial={{ scale: 0, opacity: 0 }}
            animate={{ scale: 1, opacity: 1 }}
            exit={{ scale: 0, opacity: 0 }}
            whileHover={{ scale: 1.1 }}
            whileTap={{ scale: 0.9 }}
            onClick={() => setIsOpen(true)}
            className="bg-accentRed hover:bg-red-500 text-white rounded-full p-3 shadow-lg shadow-red-900/30 transition-all duration-200"
          >
            <MessageCircle className="h-6 w-6" />
          </motion.button>
        )}
      </AnimatePresence>

      {/* Chat Panel */}
      <AnimatePresence>
        {isOpen && (
          <motion.div
            initial={{ scale: 0.8, opacity: 0, y: 20 }}
            animate={{ scale: 1, opacity: 1, y: 0 }}
            exit={{ scale: 0.8, opacity: 0, y: 20 }}
            transition={{ type: "spring", stiffness: 300, damping: 30 }}
            className="bg-bgElevated border border-borderSoft rounded-xl2 shadow-2xl shadow-black/50 w-80 h-96 flex flex-col"
          >
            {/* Header */}
            <div className="flex items-center justify-between p-4 border-b border-borderSoft">
              <div className="flex items-center gap-2">
                <div className="flex h-8 w-8 items-center justify-center rounded-full bg-accentRed/20">
                  <Bot className="h-4 w-4 text-accentRed" />
                </div>
                <div>
                  <p className="font-semibold text-whitePrimary">Telemetry Assistant</p>
                  <p className="text-xs text-whiteMuted">AI-powered analysis</p>
                </div>
              </div>
              <button
                onClick={() => setIsOpen(false)}
                className="text-whiteMuted hover:text-whitePrimary transition-colors"
              >
                <X className="h-4 w-4" />
              </button>
            </div>

            {/* Messages */}
            <div className="flex-1 overflow-y-auto p-4 space-y-3">
              {messages.map((message) => (
                <motion.div
                  key={message.id}
                  initial={{ opacity: 0, y: 10 }}
                  animate={{ opacity: 1, y: 0 }}
                  className={`flex ${message.sender === 'user' ? 'justify-end' : 'justify-start'}`}
                >
                  <div
                    className={`max-w-[80%] rounded-lg px-3 py-2 text-sm ${
                      message.sender === 'user'
                        ? 'bg-accentRed text-white'
                        : 'bg-bgSecondary text-whitePrimary'
                    }`}
                  >
                    {message.text}
                  </div>
                </motion.div>
              ))}
              
              {/* Typing Indicator */}
              {isTyping && (
                <motion.div
                  initial={{ opacity: 0, y: 10 }}
                  animate={{ opacity: 1, y: 0 }}
                  className="flex justify-start"
                >
                  <div className="bg-bgSecondary rounded-lg px-3 py-2">
                    <div className="flex gap-1">
                      <div className="w-2 h-2 bg-whiteMuted rounded-full animate-bounce" style={{ animationDelay: '0ms' }}></div>
                      <div className="w-2 h-2 bg-whiteMuted rounded-full animate-bounce" style={{ animationDelay: '150ms' }}></div>
                      <div className="w-2 h-2 bg-whiteMuted rounded-full animate-bounce" style={{ animationDelay: '300ms' }}></div>
                    </div>
                  </div>
                </motion.div>
              )}
              <div ref={messagesEndRef} />
            </div>

            {/* Input */}
            <div className="p-4 border-t border-borderSoft">
              <div className="flex gap-2">
                <input
                  type="text"
                  value={inputValue}
                  onChange={(e) => setInputValue(e.target.value)}
                  onKeyPress={handleKeyPress}
                  placeholder="Ask about telemetry data..."
                  className="flex-1 bg-bgPrimary border border-borderSoft rounded-lg px-3 py-2 text-sm text-whitePrimary placeholder-whiteMuted focus:outline-none focus:border-accentRed"
                  disabled={isTyping}
                />
                <button
                  onClick={handleSendMessage}
                  disabled={!inputValue.trim() || isTyping}
                  className="bg-accentRed hover:bg-red-500 disabled:bg-gray-700 disabled:cursor-not-allowed text-white rounded-lg p-2 transition-colors"
                >
                  <Send className="h-4 w-4" />
                </button>
              </div>
              <p className="text-xs text-whiteMuted mt-2 text-center">
                Powered by DeltaBox AI • Mock integration
              </p>
            </div>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
};

export default TelemetryChatbot;
