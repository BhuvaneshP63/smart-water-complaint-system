
import { useEffect, useState } from 'react'
import API from "../api";

function ComplaintPhoto({ complaintId }) {
  const [photoUrl, setPhotoUrl] = useState('')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(false)
  const [expanded, setExpanded] = useState(false)

  useEffect(() => {
    let objectUrl = null
    let cancelled = false

    const fetchPhoto = async () => {
      try {
        setLoading(true)
        setError(false)

        const response = await API.get(
          `/complaints/${complaintId}/photo`,
          { responseType: 'blob' }
        )

        objectUrl = URL.createObjectURL(response.data)

        if (!cancelled) {
          setPhotoUrl(objectUrl)
        }
      } catch {
        if (!cancelled) {
          setError(true)
        }
      } finally {
        if (!cancelled) {
          setLoading(false)
        }
      }
    }

    fetchPhoto()

    return () => {
      cancelled = true
      if (objectUrl) {
        URL.revokeObjectURL(objectUrl)
      }
    }
  }, [complaintId])

  if (loading) {
    return <p>Loading complaint photo...</p>
  }

  if (error) {
    return <p>Unable to load complaint photo.</p>
  }

  return (
    <div className="complaint-photo">
      <h4>Complaint Photo</h4>
      <img
        src={photoUrl}
        alt={`Complaint ${complaintId}`}
        style={{
          width: '220px',
          maxWidth: '100%',
          height: '160px',
          objectFit: 'cover',
          borderRadius: '8px',
          cursor: 'zoom-in'
        }}
        onClick={() => setExpanded(true)}
      />
      {expanded && <div onClick={() => setExpanded(false)} style={{ position: 'fixed', inset: 0, zIndex: 10000, background: 'rgba(0,0,0,.86)', display: 'grid', placeItems: 'center', padding: 24, cursor: 'zoom-out' }}>
        <button aria-label="Close photo" onClick={() => setExpanded(false)} style={{ position: 'absolute', top: 18, right: 24, color: 'white', background: 'transparent', border: 0, fontSize: 32, cursor: 'pointer' }}>×</button>
        <img src={photoUrl} alt={`Expanded complaint ${complaintId}`} onClick={event => event.stopPropagation()} style={{ maxWidth: '92vw', maxHeight: '90vh', objectFit: 'contain', borderRadius: 8 }} />
      </div>}
    </div>
  )
}

export default ComplaintPhoto
