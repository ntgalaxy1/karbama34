S.lang='fa';
S.form={jobs:['chef'],trLevel:60,cc:90,first:"علی",last:"رضایی",father:"حسین",nationalId:"0079123456",phone:"05321234567",
  city:"تهران",district:"تهران",address:"تهران",idPhoto:"data:,x",bodyPhoto:"data:,y"};
S.formErr={}; S.regStep=3; go('register'); openUndertaking();
console.log('draft after open:', JSON.stringify(S.undDraft));
setUndFee('');
console.log('after empty:', JSON.stringify(S.undDraft));
S.undDraft.payerCard='6037991122334455'; S.undDraft.sig='data:,sig'; S.undDraft.signed=true;
window.__sigHasInk=true;
undConfirm();
console.log('after undConfirm(empty fee):', 'draft=', JSON.stringify(S.undDraft), 'und=', !!S.form.undertaking);
setUndFee('7500');
console.log('after setUndFee 7500: draft=', JSON.stringify(S.undDraft));
undConfirm();
console.log('after undConfirm(7500): und=', JSON.stringify(S.form.undertaking), 'fee=', S.form.fee);
