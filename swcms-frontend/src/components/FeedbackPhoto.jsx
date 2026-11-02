
import { useEffect, useState } from 'react'
import API from '../api'

function FeedbackPhoto({ feedbackId, endpoint }) {
  const [photoUrl, setPhotoUrl] = useState('')
  const [error, setError] = useState('')
  const [expanded, setExpanded] = useState(false)

  useEffect(() => {
    let active = true
    let objectUrl = null

    const loadPhoto = async () => {
      try {
        const response = await API.get(
          endpoint || `/feedback/${feedbackId}/photo`,
          { responseType: 'blob' }
        )

        const url = URL.createObjectURL(response.data)

        if (active) {
          objectUrl = url
          setPhotoUrl(url)
        } else {
          URL.revokeObjectURL(url)
        }
      } catch {
        if (active) {
          setError('Unable to load feedback photo.')
        }
      }
    }

    if (feedbackId) loadPhoto()

    return () => {
      active = false
      if (objectUrl) URL.revokeObjectURL(objectUrl)
    }
  }, [feedbackId, endpoint])

  useEffect(() => {
    const handleKeyDown = (event) => {
      if (event.key === 'Escape') {
        setExpanded(false)
      }
    }

    window.addEventListener('keydown', handleKeyDown)

    return () => {
      window.removeEventListener('keydown', handleKeyDown)
    }
  }, [])

  if (error) return <p>{error}</p>
  if (!photoUrl) return null

  return (
    <>
      <div style={{ marginTop: '10px' }}>
        <strong>Feedback Photo:</strong>
        <br />

        <img
          src={photoUrl}
          alt="Feedback attachment"
          onClick={() => setExpanded(true)}
          style={{
            width: '65px',
            height: '110px',
            objectFit: 'cover',
            borderRadius: '8px',
            marginTop: '8px',
            cursor: 'pointer',
            transition: 'transform 0.2s'
          }}
          title="Click to expand"
        />
      </div>

      {expanded && (
        <div
          onClick={() => setExpanded(false)}
          style={{
            position: 'fixed',
            inset: 0,
            backgroundColor: 'rgba(0, 0, 0, 0.85)',
            display: 'flex',
            justifyContent: 'center',
            alignItems: 'center',
            zIndex: 9999,
            padding: '20px',
            cursor: 'zoom-out'
          }}
        >
          <button
            onClick={() => setExpanded(false)}
            style={{
              position: 'absolute',
              top: '20px',
              right: '30px',
              fontSize: '32px',
              color: 'white',
              background: 'transparent',
              border: 'none',
              cursor: 'pointer'
            }}
          >
            &times;
          </button>

          <img
            src={photoUrl}
            alt="Expanded feedback attachment"
            onClick={(event) => event.stopPropagation()}
            style={{
              maxWidth: '90vw',
              maxHeight: '90vh',
              objectFit: 'contain',
              borderRadius: '10px',
              cursor: 'default'
            }}
          />
        </div>
      )}
    </>
  )
}

export default FeedbackPhoto
