function selectRow(type, id) {
  selected[type] = selected[type] === id ? null : id;
  renderAll();
}

function updateActionButtons(type) {
  const hasSel = selected[type] !== null;
  document.getElementById(`btn-edit-${type}`).disabled = !hasSel;
  document.getElementById(`btn-del-${type}`).disabled = !hasSel;
}

function deleteSelected(type) {
  if (!selected[type]) return;
  const key = type === 'cliente' ? 'clientes' : type === 'animal' ? 'animais' : 'vacinas';
  db[key] = db[key].filter(item => item.id !== selected[type]);
  selected[type] = null;
  persist();
}

function editSelected(type) {
  if (!selected[type]) return;
  openModal(type, selected[type]);
}

function showModalError(msg) {
  const errEl = document.getElementById('modal-error');
  errEl.innerText = msg;
  errEl.classList.remove('hidden');
}

function hideModalError() {
  const errEl = document.getElementById('modal-error');
  errEl.innerText = '';
  errEl.classList.add('hidden');
}

function openModal(type, id = null) {
  currentModalType = type;
  editingId = id;
  hideModalError();

  const fieldsDiv = document.getElementById('modal-fields');
  const title = document.getElementById('modal-title');
  const subtitle = document.getElementById('modal-subtitle');
  const icon = document.getElementById('modal-icon');

  if (type === 'cliente') {
    const item = id ? db.clientes.find(c => c.id === id) : {};
    title.innerText = id ? 'Editar Cliente' : 'Novo Cliente';
    subtitle.innerText = 'Preencha os dados do produtor e da fazenda.';
    icon.setAttribute('data-lucide', 'user-plus');

    fieldsDiv.innerHTML = `
      <div>
        <label class="text-xs font-semibold text-slate-700 block mb-1">Nome completo</label>
        <input required name="nome" value="${item.nome || ''}" class="w-full px-3.5 py-2 text-sm border border-slate-200 rounded-lg focus:outline-none focus:border-emerald-500">
      </div>
      <div class="grid grid-cols-1 sm:grid-cols-2 gap-3.5">
        <div>
          <label class="text-xs font-semibold text-slate-700 block mb-1">CPF</label>
          <input required name="cpf" value="${item.cpf || ''}" minlength="14" maxlength="14" inputmode="numeric" oninput="formatCpfInput(this)" placeholder="___.___.___-__" class="w-full px-3.5 py-2 text-sm border border-slate-200 rounded-lg focus:outline-none focus:border-emerald-500">
        </div>
        <div>
          <label class="text-xs font-semibold text-slate-700 block mb-1">Data de Nascimento</label>
          <input required name="nascimento" value="${item.nascimento ? formatDateBR(item.nascimento) : ''}" minlength="10" maxlength="10" inputmode="numeric" oninput="formatDateInput(this)" placeholder="__/__/____" class="w-full px-3.5 py-2 text-sm border border-slate-200 rounded-lg focus:outline-none focus:border-emerald-500">
        </div>
      </div>
      <div>
        <label class="text-xs font-semibold text-slate-700 block mb-1">Nome da Fazenda</label>
        <input required name="fazenda" value="${item.fazenda || ''}" class="w-full px-3.5 py-2 text-sm border border-slate-200 rounded-lg focus:outline-none focus:border-emerald-500">
      </div>
      <div>
        <label class="text-xs font-semibold text-slate-700 block mb-1">Endereço</label>
        <input required name="endereco" value="${item.endereco || ''}" class="w-full px-3.5 py-2 text-sm border border-slate-200 rounded-lg focus:outline-none focus:border-emerald-500">
        <p class="text-[11px] text-slate-400 mt-1">Ex.: Rodovia SC-390, km 12 - Tubarão/SC</p>
      </div>
    `;
  } else if (type === 'animal') {
    const item = id ? db.animais.find(a => a.id === id) : {};
    title.innerText = id ? 'Editar Animal' : 'Novo Animal';
    subtitle.innerText = 'Informe o dono e os dados do animal.';
    icon.setAttribute('data-lucide', 'beef');

    const donoFazendaOptions = db.clientes.map(c => {
      const val = `${c.nome}|||${c.fazenda}`;
      const isSelected = (item.dono === c.nome && item.fazenda === c.fazenda) || (!item.dono && db.clientes[0]?.id === c.id);
      return `<option value="${val}" ${isSelected ? 'selected' : ''}>${c.nome} — ${c.fazenda}</option>`;
    }).join('');

    const racasPadrao = ['Nelore', 'Angus', 'Brahman', 'Gir', 'Tabapuã', 'Hereford', 'Senepol', 'Girolando'];
    if (item.raca && !racasPadrao.includes(item.raca)) racasPadrao.unshift(item.raca);

    fieldsDiv.innerHTML = `
      <div>
        <label class="text-xs font-semibold text-slate-700 block mb-1">Dono (Cliente / Fazenda)</label>
        <select required name="donoFazenda" class="w-full px-3.5 py-2 text-sm border border-slate-200 rounded-lg bg-white focus:outline-none focus:border-emerald-500">
          ${donoFazendaOptions || '<option value="Produtor|||Fazenda Principal">Produtor — Fazenda Principal</option>'}
        </select>
      </div>
      <div>
        <label class="text-xs font-semibold text-slate-700 block mb-1">Apelido do Animal</label>
        <input required name="apelido" value="${item.apelido || ''}" class="w-full px-3.5 py-2 text-sm border border-slate-200 rounded-lg focus:outline-none focus:border-emerald-500">
      </div>
      <div class="grid grid-cols-1 sm:grid-cols-2 gap-3.5">
        <div>
          <label class="text-xs font-semibold text-slate-700 block mb-1">Raça</label>
          <select name="raca" class="w-full px-3.5 py-2 text-sm border border-slate-200 rounded-lg bg-white focus:outline-none focus:border-emerald-500">
            ${racasPadrao.map(r => `<option value="${r}" ${item.raca === r ? 'selected' : ''}>${r}</option>`).join('')}
          </select>
        </div>
        <div>
          <label class="text-xs font-semibold text-slate-700 block mb-1">Gênero</label>
          <select name="genero" class="w-full px-3.5 py-2 text-sm border border-slate-200 rounded-lg bg-white focus:outline-none focus:border-emerald-500">
            <option value="Fêmea" ${item.genero === 'Fêmea' ? 'selected' : ''}>Fêmea</option>
            <option value="Macho" ${item.genero === 'Macho' ? 'selected' : ''}>Macho</option>
          </select>
        </div>
      </div>
      <div>
        <label class="text-xs font-semibold text-slate-700 block mb-1">Data de Nascimento</label>
        <input required name="nascimento" value="${item.nascimento ? formatDateBR(item.nascimento) : ''}" minlength="10" maxlength="10" inputmode="numeric" oninput="formatDateInput(this)" placeholder="__/__/____" class="w-full px-3.5 py-2 text-sm border border-slate-200 rounded-lg focus:outline-none focus:border-emerald-500">
        <p class="text-[11px] text-slate-400 mt-1">Formato dd/mm/aaaa (não pode ser futura)</p>
      </div>
    `;
  } else if (type === 'vacina') {
    const item = id ? db.vacinas.find(v => v.id === id) : {};
    title.innerText = id ? 'Editar Aplicação de Vacina' : 'Registrar Aplicação de Vacina';
    subtitle.innerText = 'Escolha o animal e informe a vacina aplicada.';
    icon.setAttribute('data-lucide', 'syringe');

    const animalOptions = db.animais.map(a => {
      const val = `${a.apelido}|||${a.dono}`;
      const isSelected = item.animal === a.apelido || (!item.animal && db.animais[0]?.id === a.id);
      return `<option value="${val}" ${isSelected ? 'selected' : ''}>#${a.id} ${a.apelido} (${a.raca}) — ${a.dono}</option>`;
    }).join('');

    const vacinasSugeridas = ['Febre Aftosa', 'Brucelose', 'Raiva', 'Clostridioses', 'Leptospirose', 'IBR / BVD'];

    fieldsDiv.innerHTML = `
      <div>
        <label class="text-xs font-semibold text-slate-700 block mb-1">Animal</label>
        <select required name="animalDono" class="w-full px-3.5 py-2 text-sm border border-slate-200 rounded-lg bg-white focus:outline-none focus:border-emerald-500">
          ${animalOptions || '<option value="Animal|||Dono">Animal — Dono</option>'}
        </select>
      </div>
      <div>
        <label class="text-xs font-semibold text-slate-700 block mb-1">Vacina</label>
        <input required list="lista-vacinas-sugeridas" name="vacina" value="${item.vacina || ''}" class="w-full px-3.5 py-2 text-sm border border-slate-200 rounded-lg focus:outline-none focus:border-emerald-500">
        <datalist id="lista-vacinas-sugeridas">
          ${vacinasSugeridas.map(v => `<option value="${v}">`).join('')}
        </datalist>
        <p class="text-[11px] text-slate-400 mt-1">Escolha da lista ou digite o nome</p>
      </div>
      <div class="grid grid-cols-1 sm:grid-cols-2 gap-3.5">
        <div>
          <label class="text-xs font-semibold text-slate-700 block mb-1">Data da Aplicação</label>
          <input required id="vacina-aplicada-input" name="aplicadaEm" value="${item.aplicadaEm ? formatDateBR(item.aplicadaEm) : '18/11/2026'}" minlength="10" maxlength="10" inputmode="numeric" oninput="formatDateInput(this)" placeholder="__/__/____" class="w-full px-3.5 py-2 text-sm border border-slate-200 rounded-lg focus:outline-none focus:border-emerald-500">
        </div>
        <div>
          <label class="text-xs font-semibold text-slate-700 block mb-1">Próxima Dose</label>
          <input required id="vacina-proxima-input" name="proximaDose" value="${item.proximaDose ? formatDateBR(item.proximaDose) : ''}" minlength="10" maxlength="10" inputmode="numeric" oninput="formatDateInput(this)" placeholder="__/__/____" class="w-full px-3.5 py-2 text-sm border border-slate-200 rounded-lg focus:outline-none focus:border-emerald-500">
        </div>
      </div>
      <div>
        <p class="text-[11px] font-semibold text-slate-500 mb-1.5">Calcular próxima dose</p>
        <div class="flex flex-wrap gap-2">
          <button type="button" onclick="addDoseDuration('30d')" class="px-2.5 py-1 text-[11px] font-semibold text-slate-700 bg-white border border-slate-200 hover:bg-slate-50 rounded-md transition">+ 30 dias</button>
          <button type="button" onclick="addDoseDuration('3m')" class="px-2.5 py-1 text-[11px] font-semibold text-slate-700 bg-white border border-slate-200 hover:bg-slate-50 rounded-md transition">+ 3 meses</button>
          <button type="button" onclick="addDoseDuration('6m')" class="px-2.5 py-1 text-[11px] font-semibold text-slate-700 bg-white border border-slate-200 hover:bg-slate-50 rounded-md transition">+ 6 meses</button>
          <button type="button" onclick="addDoseDuration('1y')" class="px-2.5 py-1 text-[11px] font-semibold text-slate-700 bg-white border border-slate-200 hover:bg-slate-50 rounded-md transition">+ 1 ano</button>
        </div>
      </div>
      <div>
        <label class="text-xs font-semibold text-slate-700 block mb-1">Quem aplicou?</label>
        <input required name="aplicadoPor" value="${item.aplicadoPor || ''}" class="w-full px-3.5 py-2 text-sm border border-slate-200 rounded-lg focus:outline-none focus:border-emerald-500">
        <p class="text-[11px] text-slate-400 mt-1">Veterinário ou Dono</p>
      </div>
    `;
  }

  lucide.createIcons();
  const overlay = document.getElementById('modal-overlay');
  overlay.classList.remove('hidden');
  overlay.classList.add('flex');
}

function closeModal() {
  hideModalError();
  const overlay = document.getElementById('modal-overlay');
  overlay.classList.add('hidden');
  overlay.classList.remove('flex');
}

function saveForm(e) {
  e.preventDefault();
  hideModalError();

  const raw = Object.fromEntries(new FormData(e.target).entries());
  const key = currentModalType === 'cliente' ? 'clientes' : currentModalType === 'animal' ? 'animais' : 'vacinas';

  let record = {};

  if (currentModalType === 'cliente') {
    if (!isValidCpf(raw.cpf)) {
      showModalError('Informe um CPF válido com exatamente 11 números.');
      return;
    }
    const nascimentoIso = parseValidDateBR(raw.nascimento);
    if (!nascimentoIso) {
      showModalError('Data de nascimento inválida. Use dd/mm/aaaa com dia, mês e ano reais.');
      return;
    }
    if (isFutureDate(nascimentoIso)) {
      showModalError('A data de nascimento não pode ser no futuro.');
      return;
    }

    const digits = raw.cpf.replace(/\D/g, '');
    const cpfFormatted = digits.replace(/^(\d{3})(\d{3})(\d{3})(\d{2})$/, '$1.$2.$3-$4');

    record = {
      nome: raw.nome.trim(),
      cpf: cpfFormatted,
      nascimento: nascimentoIso,
      fazenda: raw.fazenda.trim(),
      endereco: raw.endereco.trim()
    };
  } else if (currentModalType === 'animal') {
    const nascimentoIso = parseValidDateBR(raw.nascimento);
    if (!nascimentoIso) {
      showModalError('Data de nascimento inválida. Use dd/mm/aaaa com dia, mês e ano reais.');
      return;
    }
    if (isFutureDate(nascimentoIso)) {
      showModalError('A data de nascimento do animal não pode ser no futuro (evita idade negativa).');
      return;
    }

    const [dono, fazenda] = (raw.donoFazenda || '|||').split('|||');
    record = {
      apelido: raw.apelido.trim(),
      raca: raw.raca,
      genero: raw.genero,
      nascimento: nascimentoIso,
      dono: dono || '',
      fazenda: fazenda || ''
    };
  } else if (currentModalType === 'vacina') {
    const aplicadaIso = parseValidDateBR(raw.aplicadaEm);
    const proximaIso = parseValidDateBR(raw.proximaDose);
    if (!aplicadaIso || !proximaIso) {
      showModalError('Datas de aplicação ou próxima dose inválidas. Use dd/mm/aaaa com datas reais.');
      return;
    }

    const [animal, dono] = (raw.animalDono || '|||').split('|||');
    record = {
      animal: animal || '',
      dono: dono || '',
      vacina: raw.vacina.trim(),
      aplicadaEm: aplicadaIso,
      proximaDose: proximaIso,
      aplicadoPor: raw.aplicadoPor.trim()
    };
  }

  if (editingId) {
    const idx = db[key].findIndex(item => item.id === editingId);
    db[key][idx] = { ...db[key][idx], ...record };
  } else {
    const nextId = db[key].length ? Math.max(...db[key].map(i => i.id)) + 1 : 1;
    db[key].push({ id: nextId, ...record });
  }

  closeModal();
  persist();
}

renderAll();
