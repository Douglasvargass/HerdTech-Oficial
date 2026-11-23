function switchTab(tab) {
  const tabs = ['inicio', 'fazendas', 'animais', 'vacinacao', 'relatorios'];
  tabs.forEach(t => {
    document.getElementById(`view-${t}`).classList.add('hidden');
    const btn = document.getElementById(`nav-${t}`);
    btn.classList.remove('tab-active');
    btn.classList.add('tab-inactive');
  });
  document.getElementById(`view-${tab}`).classList.remove('hidden');
  const activeBtn = document.getElementById(`nav-${tab}`);
  activeBtn.classList.remove('tab-inactive');
  activeBtn.classList.add('tab-active');
  // Barra de abas inferior (celular/tablet)
  document.querySelectorAll('[data-aba]').forEach(b => {
    b.classList.toggle('aba-ativa', b.dataset.aba === tab);
  });
  window.scrollTo(0, 0);
}

function renderAll() {
  renderClientes();
  renderAnimais();
  renderVacinas();
  renderDashboard();
  renderRelatorios();
  lucide.createIcons();
}

function renderBarList(containerId, entries, barColorClass) {
  const el = document.getElementById(containerId);
  if (!el) return;
  if (!entries.length) {
    el.innerHTML = `<p class="text-xs text-slate-400 py-2">Nenhum registro encontrado.</p>`;
    return;
  }
  const maxVal = Math.max(...entries.map(([, v]) => v), 1);
  el.innerHTML = entries.map(([label, qtd]) => {
    const pct = qtd > 0 ? Math.round((qtd / maxVal) * 100) : 0;
    return `
      <div class="flex items-center gap-4 text-xs">
        <span class="w-28 font-medium text-slate-700 truncate">${label}</span>
        <div class="flex-1 bg-slate-100 h-2 rounded-full overflow-hidden">
          <div class="${barColorClass} h-full rounded-full transition-all duration-300" style="width: ${pct}%"></div>
        </div>
        <span class="font-bold text-slate-800 w-5 text-right">${qtd}</span>
      </div>
    `;
  }).join('');
}

function renderRelatorios() {
  const fazendasMap = {};
  db.clientes.forEach(c => {
    if (c.fazenda && !(c.fazenda in fazendasMap)) fazendasMap[c.fazenda] = 0;
  });
  db.animais.forEach(a => {
    if (a.fazenda) fazendasMap[a.fazenda] = (fazendasMap[a.fazenda] || 0) + 1;
  });
  renderBarList('rel-animais-fazenda', Object.entries(fazendasMap), 'bg-emerald-600');

  const situacaoMap = {};
  db.vacinas.forEach(v => {
    const badge = calcVacinaStatus(v.proximaDose).badge;
    situacaoMap[badge] = (situacaoMap[badge] || 0) + 1;
  });
  const sitEntries = Object.entries(situacaoMap);
  const sitEl = document.getElementById('rel-situacao-sanitaria');
  if (sitEl) {
    if (!sitEntries.length) {
      sitEl.innerHTML = `<p class="text-xs text-slate-400 py-2">Nenhuma vacina registrada.</p>`;
    } else {
      const maxSit = Math.max(...sitEntries.map(([, v]) => v), 1);
      sitEl.innerHTML = sitEntries.map(([label, qtd]) => {
        const color = label === 'Em dia' ? 'bg-emerald-600' : label === 'Atrasada' ? 'bg-red-500' : 'bg-sky-500';
        const pct = qtd > 0 ? Math.round((qtd / maxSit) * 100) : 0;
        return `
          <div class="flex items-center gap-4 text-xs">
            <span class="w-28 font-medium text-slate-700 truncate">${label}</span>
            <div class="flex-1 bg-slate-100 h-2 rounded-full overflow-hidden">
              <div class="${color} h-full rounded-full transition-all duration-300" style="width: ${pct}%"></div>
            </div>
            <span class="font-bold text-slate-800 w-5 text-right">${qtd}</span>
          </div>
        `;
      }).join('');
    }
  }

  const racasMap = {};
  db.animais.forEach(a => {
    racasMap[a.raca] = (racasMap[a.raca] || 0) + 1;
  });
  renderBarList('rel-composicao-raca', Object.entries(racasMap), 'bg-[#002f56]');

  const vacinasMap = {};
  db.vacinas.forEach(v => {
    vacinasMap[v.vacina] = (vacinasMap[v.vacina] || 0) + 1;
  });
  renderBarList('rel-vacinas-aplicadas', Object.entries(vacinasMap), 'bg-sky-500');
}

function renderDashboard() {
  const fazendasUnicas = [...new Set(db.clientes.map(c => c.fazenda).filter(Boolean))];
  const pendentesList = db.vacinas.filter(v => calcVacinaStatus(v.proximaDose).pending);

  document.getElementById('kpi-animais').innerText = db.animais.length;
  document.getElementById('kpi-fazendas').innerText = fazendasUnicas.length;
  document.getElementById('kpi-clientes').innerText = db.clientes.length;
  document.getElementById('kpi-vacinas').innerText = db.vacinas.length;
  document.getElementById('kpi-pendentes').innerText = pendentesList.length;
  document.getElementById('badge-notificacao').innerText = pendentesList.length;
  document.getElementById('footer-stats').innerText = `${db.clientes.length} clientes, ${db.animais.length} animais, ${db.vacinas.length} vacinas`;

  const proxContainer = document.getElementById('lista-proximas-vacinas');
  if (pendentesList.length === 0) {
    proxContainer.innerHTML = `<p class="text-xs text-slate-400 py-4">Nenhuma dose pendente para os próximos 30 dias.</p>`;
  } else {
    proxContainer.innerHTML = pendentesList.map(v => {
      const st = calcVacinaStatus(v.proximaDose);
      const animalObj = db.animais.find(a => a.apelido === v.animal);
      return `
        <div class="flex items-center justify-between p-3 rounded-xl bg-slate-50/70 border border-slate-100">
          <div class="flex items-center gap-3">
            <div class="w-10 h-10 rounded-lg bg-sky-100/80 text-sky-600 flex items-center justify-center">
              <i data-lucide="syringe" class="w-4 h-4"></i>
            </div>
            <div>
              <p class="text-xs font-bold text-slate-800">${v.vacina}</p>
              <p class="text-[11px] text-slate-400">${v.animal} • ${animalObj ? animalObj.fazenda : ''}</p>
            </div>
          </div>
          <div class="text-right">
            <span class="inline-block ${st.cls} text-[10px] font-bold px-2.5 py-0.5 rounded-full mb-1">${st.badge}</span>
            <p class="text-[11px] text-slate-400">${formatDateBR(v.proximaDose)} <strong class="text-slate-600">(${st.dias})</strong></p>
          </div>
        </div>
      `;
    }).join('');
  }

  const racasMap = {};
  let femeas = 0, machos = 0;
  db.animais.forEach(a => {
    racasMap[a.raca] = (racasMap[a.raca] || 0) + 1;
    if (a.genero === 'Fêmea') femeas++; else machos++;
  });

  document.getElementById('count-femeas').innerText = femeas;
  document.getElementById('count-machos').innerText = machos;

  const totalAnimais = db.animais.length || 1;
  const racasContainer = document.getElementById('composicao-racas');
  racasContainer.innerHTML = Object.entries(racasMap).map(([raca, qtd]) => {
    const pct = qtd > 0 ? Math.round((qtd / totalAnimais) * 100) : 0;
    return `
      <div class="flex items-center gap-4 text-xs">
        <span class="w-24 font-semibold text-slate-700 truncate">${raca}</span>
        <div class="flex-1 bg-slate-100 h-2.5 rounded-full overflow-hidden">
          <div class="bg-emerald-600 h-full rounded-full" style="width: ${pct}%"></div>
        </div>
        <span class="font-bold text-slate-800 w-6 text-right">${qtd}</span>
      </div>
    `;
  }).join('') || `<p class="text-xs text-slate-400">Nenhum animal cadastrado.</p>`;
}

function renderClientes() {
  const q = document.getElementById('search-clientes').value.toLowerCase();
  const filtered = db.clientes.filter(c => Object.values(c).some(val => String(val).toLowerCase().includes(q)));
  document.getElementById('count-reg-clientes').innerText = `${filtered.length} registro${filtered.length !== 1 ? 's' : ''}`;

  document.getElementById('tbody-clientes').innerHTML = filtered.map(c => `
    <tr onclick="selectRow('cliente', ${c.id})" class="cursor-pointer hover:bg-slate-50 transition ${selected.cliente === c.id ? 'selected-row' : ''}">
      <td class="py-3.5 px-4 text-slate-400 font-medium">#${c.id}</td>
      <td class="py-3.5 px-4 font-semibold text-slate-800">${c.nome}</td>
      <td class="py-3.5 px-4">${c.cpf}</td>
      <td class="py-3.5 px-4">${formatDateBR(c.nascimento)}</td>
      <td class="py-3.5 px-4">${c.fazenda}</td>
      <td class="py-3.5 px-4">${c.endereco}</td>
    </tr>
  `).join('');
  updateActionButtons('cliente');
}

function renderAnimais() {
  const q = document.getElementById('search-animais').value.toLowerCase();
  const filtered = db.animais.filter(a => Object.values(a).some(val => String(val).toLowerCase().includes(q)));
  document.getElementById('count-reg-animais').innerText = `${filtered.length} registro${filtered.length !== 1 ? 's' : ''}`;

  document.getElementById('tbody-animais').innerHTML = filtered.map(a => `
    <tr onclick="selectRow('animal', ${a.id})" class="cursor-pointer hover:bg-slate-50 transition ${selected.animal === a.id ? 'selected-row' : ''}">
      <td class="py-3.5 px-4 text-slate-400 font-medium">#${a.id}</td>
      <td class="py-3.5 px-4 font-semibold text-slate-800">${a.apelido}</td>
      <td class="py-3.5 px-4">${a.raca}</td>
      <td class="py-3.5 px-4">${a.genero}</td>
      <td class="py-3.5 px-4">${formatDateBR(a.nascimento)}</td>
      <td class="py-3.5 px-4">${calcAge(a.nascimento)}</td>
      <td class="py-3.5 px-4">${a.dono}</td>
      <td class="py-3.5 px-4">${a.fazenda}</td>
    </tr>
  `).join('');
  updateActionButtons('animal');
}

function renderVacinas() {
  const q = document.getElementById('search-vacinas').value.toLowerCase();
  const filtered = db.vacinas.filter(v => Object.values(v).some(val => String(val).toLowerCase().includes(q)));
  document.getElementById('count-reg-vacinas').innerText = `${filtered.length} registro${filtered.length !== 1 ? 's' : ''}`;

  document.getElementById('tbody-vacinas').innerHTML = filtered.map(v => {
    const st = calcVacinaStatus(v.proximaDose);
    return `
      <tr onclick="selectRow('vacina', ${v.id})" class="cursor-pointer hover:bg-slate-50 transition ${selected.vacina === v.id ? 'selected-row' : ''}">
        <td class="py-3.5 px-4 text-slate-400 font-medium">#${v.id}</td>
        <td class="py-3.5 px-4 font-semibold text-slate-800">${v.animal}</td>
        <td class="py-3.5 px-4">${v.dono}</td>
        <td class="py-3.5 px-4">${v.vacina}</td>
        <td class="py-3.5 px-4 font-medium">${formatDateBR(v.aplicadaEm)}</td>
        <td class="py-3.5 px-4 font-medium">${formatDateBR(v.proximaDose)}</td>
        <td class="py-3.5 px-4">${st.dias}</td>
        <td class="py-3.5 px-4">${v.aplicadoPor}</td>
        <td class="py-3.5 px-4"><span class="${st.cls} text-[10px] font-bold px-2.5 py-0.5 rounded-full">${st.badge}</span></td>
      </tr>
    `;
  }).join('');
  updateActionButtons('vacina');
}
