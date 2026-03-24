import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import { ChakraProvider, defaultSystem } from '@chakra-ui/react'
import './index.css'
import App from './App.tsx'

createRoot(document.getElementById('root')!).render(
  <ChakraProvider value={defaultSystem}>
    <BrowserRouter>
      <App />
    </BrowserRouter>
  </ChakraProvider>
)
