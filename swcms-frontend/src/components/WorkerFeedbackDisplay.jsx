import { useEffect, useState } from 'react'
import API from '../api'
import FeedbackPhoto from './FeedbackPhoto'

export default function WorkerFeedbackDisplay({ complaintId }) {
  const [feedback, setFeedback] = useState(null)
  const [missing, setMissing] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    let active = true
    API.get(`/user/complaints/${complaintId}/worker-feedback`)
      .then(response => { if (active) setFeedback(response.data) })
      .catch(requestError => {
        if (active && requestError.response?.status === 404) setMissing(true)
        else if (active) setError('Unable to load worker feedback.')
      })
    return () => { active = false }
  }, [complaintId])

  if (!feedback) return missing ? <p>No worker feedback submitted.</p> : error ? <p className="error">{error}</p> : <p>Loading worker feedback...</p>
  return (
    <section className="feedback-card">
      <h3>Worker Feedback</h3>
      <p><strong>Rating:</strong> {'⭐'.repeat(Number(feedback.rating) || 0)} ({feedback.rating}/5)</p>
      <p><strong>Comment:</strong> {feedback.comment || 'No comment'}</p>
      {feedback.photoPath && <FeedbackPhoto feedbackId={complaintId} endpoint={`/user/complaints/${complaintId}/worker-feedback/photo`} />}
    </section>
  )
}
