import React from 'react';

function App() {
  return (
      <div style={{ padding: '50px', textAlign: 'center', backgroundColor: '#f0f4f8', minHeight: '100vh' }}>
        <h1 style={{ color: '#1a365d' }}>AI Complaint System</h1>
        <div style={{ backgroundColor: 'white', padding: '30px', borderRadius: '15px', display: 'inline-block', boxShadow: '0 4px 6px rgba(0,0,0,0.1)' }}>
          <textarea placeholder="Describe your issue..." style={{ width: '300px', height: '100px', display: 'block', marginBottom: '20px', padding: '10px' }} />
          <button style={{ backgroundColor: '#3182ce', color: 'white', padding: '10px 20px', border: 'none', borderRadius: '5px', cursor: 'pointer', fontWeight: 'bold' }}>
            Submit to AI
          </button>
          <button style={{ marginLeft: '10px', padding: '10px 20px', borderRadius: '5px', cursor: 'pointer' }}>
            Clear
          </button>
        </div>
      </div>
  );
}

export default App;