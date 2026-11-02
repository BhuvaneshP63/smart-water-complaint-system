export default function Pagination({ page, total, pageSize, onChange }) {
  const pages=Math.max(1,Math.ceil(total/pageSize))
  if(total<=pageSize)return null
  return <div className="pagination-bar"><span>{total} records · Page {page} of {pages}</span><div><button className="refresh-btn" disabled={page<=1} onClick={()=>onChange(page-1)}>Previous</button><button className="refresh-btn" disabled={page>=pages} onClick={()=>onChange(page+1)}>Next</button></div></div>
}
