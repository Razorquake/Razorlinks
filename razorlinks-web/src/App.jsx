import './App.css'
import {BrowserRouter, Route, Routes} from "react-router-dom";
import LandingPage from "./components/LandingPage.jsx";
import AboutPage from "./components/AboutPage.jsx";

function App() {

  return (
      <>
          <BrowserRouter>
              <Routes>
                  <Route path='/' element={<LandingPage/>}/>
                  <Route path='/about' element={<AboutPage/>}/>
              </Routes>
          </BrowserRouter>
      </>
  )
}

export default App
