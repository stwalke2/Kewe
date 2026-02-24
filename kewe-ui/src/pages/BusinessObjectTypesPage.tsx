import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { fetchBusinessObjectTypes } from '../api';
import type { BusinessObjectType } from '../api/types';

export function BusinessObjectTypesPage() {
  const [types, setTypes] = useState<BusinessObjectType[]>([]);
  const navigate = useNavigate();
  useEffect(() => { void fetchBusinessObjectTypes().then(setTypes); }, []);
  return <section className="page-section"><h2>Business Object Types</h2><table><thead><tr><th>Code</th><th>Name</th><th>Category</th><th>Status</th><th>Actions</th></tr></thead><tbody>{types.map((t)=><tr key={t.id}><td>{t.code}</td><td>{t.name}</td><td>{t.objectKind}</td><td>{t.status}</td><td><button className="btn btn-secondary" onClick={()=>navigate(`/business-object-types/${t.code}`)}>View/Edit</button></td></tr>)}</tbody></table></section>;
}
