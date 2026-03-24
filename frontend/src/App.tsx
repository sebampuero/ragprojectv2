import { Routes, Route } from 'react-router-dom'
import Landing from './pages/Landing'
import ChatPage from './pages/ChatPage'

function App() {
  return (
    <Routes>
      <Route path="/" element={<Landing />} />
      <Route path="/chat" element={<ChatPage />} />
    </Routes>
  )
}

export default App
