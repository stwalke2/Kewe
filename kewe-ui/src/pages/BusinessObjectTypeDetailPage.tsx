import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { fetchBusinessObjectType, updateBusinessObjectType } from '../api';
import type { BusinessObjectType } from '../api/types';
import { HelpTip } from '../ui/help/HelpTip';

type Tab = 'basic' | 'accounting' | 'roles';
const helpFields = ['budgetRequired','budgetControlLevel','allowBudgetOverride','allowCarryforward','allowExpensePosting','allowRevenuePosting','enableEncumbrance','enablePreEncumbrance','defaultLedgerAccountId','defaultFunctionId','restrictionType','netAssetClassMapping','idcEligible','sponsorApprovalRequired','allowCostTransfer','cashManaged','investmentManaged','unitized','capitalizable','defaultDepreciationProfile'] as const;

export function BusinessObjectTypeDetailPage() {
  const { code='' } = useParams();
  const [tab,setTab]=useState<Tab>('basic');
  const [model,setModel]=useState<BusinessObjectType|null>(null);
  useEffect(()=>{void fetchBusinessObjectType(code).then(setModel);},[code]);
  if(!model) return <p>Loading…</p>;
  return <section className='page-section'><div className='page-header-row'><h2>Business Object Type: {model.code}</h2><button className='btn btn-primary' onClick={()=>void updateBusinessObjectType(model.code, model)}>Save</button></div>
    <div className='segmented-control'><button className={tab==='basic'?'segment active':'segment'} onClick={()=>setTab('basic')}>Basic Setup</button><button className={tab==='accounting'?'segment active':'segment'} onClick={()=>setTab('accounting')}>Accounting/Budget Setup</button><button className={tab==='roles'?'segment active':'segment'} onClick={()=>setTab('roles')}>Roles Template</button></div>
    {tab==='basic' && <div className='form-grid'><label>Name<input value={model.name} onChange={(e)=>setModel({...model,name:e.target.value})}/></label><label>Category<input value={model.objectKind} onChange={(e)=>setModel({...model,objectKind:e.target.value})}/></label></div>}
    {tab==='accounting' && <div><p>This configuration defines default accounting and budget behavior for all objects of this type. Individual objects may override fields where allowed.</p><div className='form-grid'>{helpFields.map((f)=><label key={f}>{f} <HelpTip term={f} />
      <input value={String(model.accountingBudgetDefaults?.[f]?.defaultValue ?? '')} onChange={(e)=>setModel({...model,accountingBudgetDefaults:{...model.accountingBudgetDefaults,[f]:{...(model.accountingBudgetDefaults?.[f]??{allowOverride:false,overrideReasonRequired:false}),defaultValue:e.target.value}}})}/>
      <span><input type='checkbox' checked={Boolean(model.accountingBudgetDefaults?.[f]?.allowOverride)} onChange={(e)=>setModel({...model,accountingBudgetDefaults:{...model.accountingBudgetDefaults,[f]:{...(model.accountingBudgetDefaults?.[f]??{overrideReasonRequired:false}),allowOverride:e.target.checked}}})}/> allow override</span>
      <span><input type='checkbox' checked={Boolean(model.accountingBudgetDefaults?.[f]?.overrideReasonRequired)} onChange={(e)=>setModel({...model,accountingBudgetDefaults:{...model.accountingBudgetDefaults,[f]:{...(model.accountingBudgetDefaults?.[f]??{allowOverride:false}),overrideReasonRequired:e.target.checked}}})}/> reason required</span>
    </label>)}</div></div>}
    {tab==='roles' && <p>Roles template placeholder.</p>}
    <button className='btn btn-primary' onClick={()=>void updateBusinessObjectType(model.code, model)}>Save</button>
  </section>;
}
