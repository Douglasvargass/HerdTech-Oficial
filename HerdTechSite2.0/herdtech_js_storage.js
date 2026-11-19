const CURRENT_DATE_ISO = '2026-11-18';

const defaultData = {
  clientes: [
    { id: 1, nome: 'wefg', cpf: '214.312.344-12', nascimento: '2020-05-12', fazenda: 'fetgwrthg', endereco: 'wrbrbrb' }
  ],
  animais: [
    { id: 1, apelido: 'sghogh', raca: 'Nelore', genero: 'Fêmea', nascimento: '2023-06-15', dono: 'wefg', fazenda: 'fetgwrthg' }
  ],
  vacinas: [
    { id: 1, animal: 'sghogh', dono: 'wefg', vacina: 'afefef', aplicadaEm: '2026-11-18', proximaDose: '2027-11-18', aplicadoPor: 'fulado' },
    { id: 2, animal: 'sghogh', dono: 'wefg', vacina: 'sdvgsdvgb', aplicadaEm: '2026-11-18', proximaDose: '2026-12-18', aplicadoPor: 'xfdbdxfb f' }
  ]
};

let db = JSON.parse(localStorage.getItem('herdtech_db')) || defaultData;
let selected = { cliente: null, animal: null, vacina: null };
let currentModalType = null;
let editingId = null;

function persist() {
  localStorage.setItem('herdtech_db', JSON.stringify(db));
  renderAll();
}

function formatCpfInput(input) {
  let digits = input.value.replace(/\D/g, '').slice(0, 11);
  if (digits.length > 9) {
    digits = digits.replace(/^(\d{3})(\d{3})(\d{3})(\d{0,2}).*/, '$1.$2.$3-$4');
  } else if (digits.length > 6) {
    digits = digits.replace(/^(\d{3})(\d{3})(\d{0,3}).*/, '$1.$2.$3');
  } else if (digits.length > 3) {
    digits = digits.replace(/^(\d{3})(\d{0,3}).*/, '$1.$2');
  }
  input.value = digits;
}

function isValidCpf(val) {
  const digits = String(val || '').replace(/\D/g, '');
  return digits.length === 11;
}

function formatDateInput(input) {
  let digits = input.value.replace(/\D/g, '').slice(0, 8);
  if (digits.length > 4) {
    digits = digits.replace(/^(\d{2})(\d{2})(\d{0,4}).*/, '$1/$2/$3');
  } else if (digits.length > 2) {
    digits = digits.replace(/^(\d{2})(\d{0,2}).*/, '$1/$2');
  }
  input.value = digits;
}

function parseValidDateBR(val) {
  const str = String(val || '').trim();
  const match = str.match(/^(\d{2})\/(\d{2})\/(\d{4})$/);
  if (!match) return null;

  const day = Number(match[1]);
  const month = Number(match[2]);
  const year = Number(match[3]);

  if (year < 1900 || year > 2100) return null;
  if (month < 1 || month > 12) return null;
  if (day < 1 || day > 31) return null;

  const dt = new Date(year, month - 1, day);
  if (
    dt.getFullYear() !== year ||
    dt.getMonth() !== month - 1 ||
    dt.getDate() !== day
  ) {
    return null;
  }

  const yyyy = String(year).padStart(4, '0');
  const mm = String(month).padStart(2, '0');
  const dd = String(day).padStart(2, '0');
  return `${yyyy}-${mm}-${dd}`;
}

function isFutureDate(isoDate) {
  return isoDate > CURRENT_DATE_ISO;
}

function formatDateBR(iso) {
  if (!iso) return '__/__/____';
  const parts = String(iso).split('-');
  if (parts.length !== 3) return iso;
  return `${parts[2]}/${parts[1]}/${parts[0]}`;
}

function parseDateFlexible(val) {
  if (!val) return '';
  const str = String(val).trim();
  if (/^\d{4}-\d{2}-\d{2}$/.test(str)) {
    const [y, m, d] = str.split('-').map(Number);
    return parseValidDateBR(`${String(d).padStart(2, '0')}/${String(m).padStart(2, '0')}/${String(y).padStart(4, '0')}`) || '';
  }
  return parseValidDateBR(str) || '';
}

function calcAge(iso) {
  const normalized = parseDateFlexible(iso);
  if (!normalized) return '-';
  const birth = new Date(normalized + 'T00:00:00');
  const now = new Date(CURRENT_DATE_ISO + 'T00:00:00');
  if (birth > now) return '0 anos e 0 meses';

  let years = now.getFullYear() - birth.getFullYear();
  let months = now.getMonth() - birth.getMonth();
  if (now.getDate() < birth.getDate()) {
    months--;
  }
  if (months < 0) {
    years--;
    months += 12;
  }
  if (years < 0) {
    years = 0;
    months = 0;
  }
  return `${years} anos e ${months} meses`;
}

function calcVacinaStatus(proximaIso) {
  const normalized = parseDateFlexible(proximaIso);
  const hoje = new Date(CURRENT_DATE_ISO + 'T00:00:00');
  const prox = new Date(normalized + 'T00:00:00');
  if (!normalized || isNaN(prox.getTime())) {
    return { dias: 'sem prazo', badge: 'Em dia', cls: 'bg-emerald-100 text-emerald-700', pending: false };
  }
  const diffDays = Math.round((prox - hoje) / (1000 * 60 * 60 * 24));
  if (diffDays < 0) return { dias: `atrasada ${Math.abs(diffDays)} dias`, badge: 'Atrasada', cls: 'bg-red-100 text-red-700', pending: true };
  if (diffDays <= 30) return { dias: `em ${diffDays} dias`, badge: 'Em breve', cls: 'bg-sky-100 text-sky-700', pending: true };
  return { dias: `em ${diffDays} dias`, badge: 'Em dia', cls: 'bg-emerald-100 text-emerald-700', pending: false };
}

function addDoseDuration(unit) {
  const baseInput = document.getElementById('vacina-aplicada-input');
  const nextInput = document.getElementById('vacina-proxima-input');
  if (!baseInput || !nextInput) return;

  const isoBase = parseDateFlexible(baseInput.value) || CURRENT_DATE_ISO;
  const [y, m, d] = isoBase.split('-').map(Number);
  const dt = new Date(y, (m || 1) - 1, d || 1);

  if (unit === '30d') dt.setDate(dt.getDate() + 30);
  if (unit === '3m') dt.setMonth(dt.getMonth() + 3);
  if (unit === '6m') dt.setMonth(dt.getMonth() + 6);
  if (unit === '1y') dt.setFullYear(dt.getFullYear() + 1);

  const dd = String(dt.getDate()).padStart(2, '0');
  const mm = String(dt.getMonth() + 1).padStart(2, '0');
  const yyyy = dt.getFullYear();
  nextInput.value = `${dd}/${mm}/${yyyy}`;
}
