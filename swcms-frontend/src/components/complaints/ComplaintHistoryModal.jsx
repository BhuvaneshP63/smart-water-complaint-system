
function ComplaintHistoryModal({
  complaint,
  history = [],
  loading,
  error,
  onClose
}) {
  if (!complaint) return null

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div
        className="modal-content history-modal"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="modal-header">
          <h2>
            Complaint History #{complaint.complaintId}
          </h2>

          <button
            className="modal-close"
            onClick={onClose}
          >
            ×
          </button>
        </div>

        {loading ? (
          <p>Loading complaint history...</p>
        ) : error ? (
          <p className="error">{error}</p>
        ) : history.length === 0 ? (
          <p>No status history available.</p>
        ) : (
          <div className="history-timeline">
            {history.map((item) => (
              <div
                className="history-item"
                key={item.historyId}
              >
                <div className="history-marker" />

                <div className="history-content">
                  <h4>
                    {item.oldStatus} → {item.newStatus}
                  </h4>

                  <p>
                    {item.remarks || "Status updated"}
                  </p>

                  <small>
                    Updated by: {item.updatedBy || "System"}
                  </small>

                  <br />

                  <small>
                    {item.updatedAt
                      ? new Date(item.updatedAt).toLocaleString()
                      : "-"}
                  </small>
                </div>
              </div>
            ))}
          </div>
        )}

        <button
          className="submit-btn"
          onClick={onClose}
        >
          Close
        </button>
      </div>
    </div>
  )
}

export default ComplaintHistoryModal
