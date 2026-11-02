
import { useEffect, useState } from 'react'
import API from '../api'
import FeedbackPhoto from './FeedbackPhoto'

function WorkerFeedbackForm({ complaint, onSuccess }) {
  const [rating, setRating] = useState(5)
  const [comment, setComment] = useState('')
  const [photo, setPhoto] = useState(null)

  const [loading, setLoading] = useState(false)
  const [checking, setChecking] = useState(true)
  const [feedbackSubmitted, setFeedbackSubmitted] = useState(false)
  const [existingFeedback, setExistingFeedback] = useState(null)

  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  useEffect(() => {
    let active = true

    const checkFeedback = async () => {
      try {
        const response = await API.get(
          `/worker/complaints/${complaint.complaintId}/feedback`
        )

        if (active) {
          setExistingFeedback(response.data)
          setFeedbackSubmitted(true)
          setError('')
        }
      } catch (err) {
        if (active) {
          const status = err.response?.status

          if (status === 404) {
            // No feedback has been submitted yet.
            setFeedbackSubmitted(false)
            setExistingFeedback(null)
            setError('')
          } else {
            setError(
              err.response?.data?.message ||
              (typeof err.response?.data === 'string'
                ? err.response.data
                : 'Unable to check existing feedback.')
            )
          }
        }
      } finally {
        if (active) setChecking(false)
      }
    }

    checkFeedback()

    return () => {
      active = false
    }
  }, [complaint.complaintId])

  const handleSubmit = async (event) => {
    event.preventDefault()
    setError('')
    setSuccess('')

    const formData = new FormData()
    formData.append('rating', rating)
    formData.append('comment', comment)

    if (photo) {
      formData.append('photo', photo)
    }

    setLoading(true)

    try {
      const response = await API.post(
        `/worker/complaints/${complaint.complaintId}/feedback`,
        formData
      )

      setExistingFeedback(response.data)
      setFeedbackSubmitted(true)
      setSuccess('Worker feedback submitted successfully!')

      setComment('')
      setPhoto(null)

      if (onSuccess) await onSuccess()
    } catch (err) {
      setError(
        err.response?.data?.message ||
        (typeof err.response?.data === 'string'
          ? err.response.data
          : 'Unable to submit feedback.')
      )
    } finally {
      setLoading(false)
    }
  }

  if (checking) return <p>Checking feedback...</p>

  if (error && !feedbackSubmitted) {
    return <p className="error">{error}</p>
  }

  if (feedbackSubmitted && existingFeedback) {
    return (
      <div className="worker-feedback-form">
        <h3>Feedback Submitted</h3>
        <p><strong>Rating:</strong> {existingFeedback.rating}/5</p>
        <p><strong>Comment:</strong> {existingFeedback.comment || 'No comment'}</p>
        {existingFeedback.photoPath && <FeedbackPhoto feedbackId={existingFeedback.feedbackId} endpoint={`/worker-feedback/${existingFeedback.feedbackId}/photo`} />}
        <p className="success">Worker feedback has already been submitted.</p>
      </div>
    )
  }

  return (
    <form className="worker-feedback-form" onSubmit={handleSubmit}>
      <h3>Submit Worker Feedback</h3>

      <label>Rating</label>
      <select
        value={rating}
        onChange={(e) => setRating(Number(e.target.value))}
      >
        <option value={5}>5 - Excellent</option>
        <option value={4}>4 - Very Good</option>
        <option value={3}>3 - Good</option>
        <option value={2}>2 - Fair</option>
        <option value={1}>1 - Poor</option>
      </select>

      <label>Comment</label>
      <textarea
        value={comment}
        onChange={(e) => setComment(e.target.value)}
        placeholder="Describe your work..."
        maxLength={1000}
      />

      <label>Feedback Photo</label>
      <input
        type="file"
        accept="image/jpeg,image/png,image/webp"
        onChange={(e) => setPhoto(e.target.files?.[0] || null)}
      />

      {error && <p className="error">{error}</p>}
      {success && <p className="success">{success}</p>}

      <button type="submit" disabled={loading}>
        {loading ? 'Submitting...' : 'Submit Feedback'}
      </button>
    </form>
  )
}

export default WorkerFeedbackForm
