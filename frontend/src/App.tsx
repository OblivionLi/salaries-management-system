import React from 'react';
import './App.css';
import SalariesScreen from "./screens/SalariesScreen";
import {Route, Routes} from "react-router-dom";

function App() {
    return (
        <Routes>
            <Route path="/" element={<SalariesScreen />} />
        </Routes>
    );
}

export default App;
