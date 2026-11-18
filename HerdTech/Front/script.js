// ==========================================================================
// BANCO DE DADOS LOCAL (LocalStorage)
// ==========================================================================
let fazendas = JSON.parse(localStorage.getItem('herdtech_fazendas')) || [
    { id: 1, nome: "Fazenda do Gui", local: "Tubarão - SC" }
];

let animais = JSON.parse(localStorage.getItem('herdtech_animais')) || [
    { id: 1, brinco: "000001", tipo: "Leite", fazendaId: 1 }
];

let vacinas = JSON.parse(localStorage.getItem('herdtech_vacinas')) || [
    { id: 1, nome: "Febre Aftosa", animalId: 1, data: "2025-06-05" }
];

// ==========================================================================
// SELETORES DO DOM
// ==========================================================================
const modalAnimal = document.getElementById('modal-animal');
const modalFazenda = document.getElementById('modal-fazenda');
const modalVacina = document.getElementById('modal-vacina');

// ==========================================================================
// SISTEMA DE ABAS (NAVEGAÇÃO)
// ==========================================================================
let abaAtual = 'inicio';

function mudarAba(aba) {
    abaAtual = aba;

    // Destaque no menu
    document.querySelectorAll('.nav-links a[data-tab]').forEach(link => {
        link.classList.toggle('active', link.dataset.tab === aba);
    });

    // Mostra/esconde cada seção ou painel conforme o data-show
    document.querySelectorAll('[data-show]').forEach(el => {
        const abas = el.dataset.show.split(' ');
        el.classList.toggle('tab-hidden', !abas.includes(aba));
    });

    // Nas abas de gestão individual, o painel ocupa a largura toda
    const dashboard = document.getElementById('dashboard');
    dashboard.classList.toggle('single-view', ['fazendas', 'animais', 'vacinas'].includes(aba));

    if (aba === 'relatorios') renderizarRelatorios();

    window.scrollTo({ top: 0, behavior: 'smooth' });
}

document.querySelectorAll('.nav-links a[data-tab]').forEach(link => {
    link.addEventListener('click', (e) => {
        e.preventDefault();
        mudarAba(link.dataset.tab);
    });
});

// ==========================================================================
// FUNÇÃO CENTRAL DE ATUALIZAÇÃO DA TELA (RENDER)
// ==========================================================================
function atualizarDados() {
    // Salva o estado atual no navegador
    localStorage.setItem('herdtech_fazendas', JSON.stringify(fazendas));
    localStorage.setItem('herdtech_animais', JSON.stringify(animais));
    localStorage.setItem('herdtech_vacinas', JSON.stringify(vacinas));

    // Atualiza os Contadores (KPIs)
    document.getElementById('kpi-total-fazendas').innerText = fazendas.length;
    document.getElementById('kpi-total-animais').innerText = animais.length;
    document.getElementById('kpi-total-vacinas').innerText = vacinas.length;

    document.getElementById('kpi-vacas-leite').innerText = animais.filter(a => a.tipo === 'Leite').length;
    document.getElementById('kpi-vacas-corte').innerText = animais.filter(a => a.tipo === 'Corte').length;

    // Atualiza os seletores dinâmicos de formulários
    document.getElementById('animal-fazenda').innerHTML = fazendas.map(f => `<option value="${f.id}">${f.nome}</option>`).join('');
    document.getElementById('vacina-animal').innerHTML = animais.map(a => `<option value="${a.id}">Brinco: ${a.brinco}</option>`).join('');

    // RENDERIZAR LISTA DE FAZENDAS
    const listaFazendas = document.getElementById('lista-fazendas-completas');
    if (fazendas.length === 0) {
        listaFazendas.innerHTML = '<p style="color:var(--text-muted)">Nenhuma fazenda cadastrada.</p>';
    } else {
        listaFazendas.innerHTML = fazendas.map(fazenda => {
            const totalAnimais = animais.filter(a => a.fazendaId == fazenda.id).length;
            return `
                <div class="activity-item" style="border-left: 4px solid var(--secondary);">
                    <div class="activity-details">
                        <p class="activity-title">${fazenda.nome}</p>
                        <span class="activity-date"><i class="fa-solid fa-location-dot"></i> ${fazenda.local}</span>
                    </div>
                    <div class="item-right">
                        <span class="status-badge hoje">${totalAnimais} Animais</span>
                        <div class="action-buttons">
                            <button class="btn-action btn-edit" onclick="abrirEditarFazenda(${fazenda.id})"><i class="fa-solid fa-pen"></i></button>
                            <button class="btn-action btn-delete" onclick="deletarFazenda(${fazenda.id})"><i class="fa-solid fa-trash"></i></button>
                        </div>
                    </div>
                </div>
            `;
        }).join('');
    }

    // RENDERIZAR LISTA DE ANIMAIS
    const listaAnimais = document.getElementById('lista-animais-recente');
    if (animais.length === 0) {
        listaAnimais.innerHTML = '<p style="color:var(--text-muted)">Nenhum animal cadastrado.</p>';
    } else {
        listaAnimais.innerHTML = animais.slice().reverse().map(animal => {
            const nomeFazenda = fazendas.find(f => f.id == animal.fazendaId)?.nome || 'Sem Fazenda';
            return `
                <div class="activity-item" style="border-left: 4px solid var(--primary);">
                    <div class="activity-details">
                        <p class="activity-title">Brinco: ${animal.brinco}</p>
                        <span class="activity-date">${nomeFazenda}</span>
                    </div>
                    <div class="item-right">
                        <span class="status-badge ${animal.tipo === 'Leite' ? 'em-breve' : 'amanha'}">${animal.tipo}</span>
                        <div class="action-buttons">
                            <button class="btn-action btn-edit" onclick="abrirEditarAnimal(${animal.id})"><i class="fa-solid fa-pen"></i></button>
                            <button class="btn-action btn-delete" onclick="deletarAnimal(${animal.id})"><i class="fa-solid fa-trash"></i></button>
                        </div>
                    </div>
                </div>
            `;
        }).join('');
    }

    // RENDERIZAR LISTA DE VACINAS APLICADAS
    const listaVacinas = document.getElementById('lista-vacinas-completas');
    if (vacinas.length === 0) {
        listaVacinas.innerHTML = '<p style="color:var(--text-muted)">Nenhuma vacina registrada.</p>';
    } else {
        listaVacinas.innerHTML = vacinas.slice().reverse().map(vacina => {
            const animal = animais.find(a => a.id == vacina.animalId);
            const brinco = animal ? animal.brinco : 'Removido';
            const dataFormatada = vacina.data.split('-').reverse().join('/');
            return `
                <div class="activity-item" style="border-left: 4px solid var(--status-breve);">
                    <div class="activity-details">
                        <p class="activity-title">${vacina.nome}</p>
                        <span class="activity-date">Aplicado em: <strong>${brinco}</strong></span>
                    </div>
                    <div class="item-right">
                        <span class="status-badge em-breve" style="background-color: #7F8C8D;">${dataFormatada}</span>
                        <div class="action-buttons">
                            <button class="btn-action btn-edit" onclick="abrirEditarVacina(${vacina.id})"><i class="fa-solid fa-pen"></i></button>
                            <button class="btn-action btn-delete" onclick="deletarVacina(${vacina.id})"><i class="fa-solid fa-trash"></i></button>
                        </div>
                    </div>
                </div>
            `;
        }).join('');
    }

    // Se estiver na aba de relatórios, mantém a análise sincronizada
    if (abaAtual === 'relatorios') renderizarRelatorios();
}

// ==========================================================================
// ABA DE RELATÓRIOS (ANÁLISE COMPLETA DOS DADOS)
// ==========================================================================
function renderizarRelatorios() {
    // 1. Animais por fazenda
    const relFazendas = document.getElementById('relatorio-fazendas');
    if (fazendas.length === 0) {
        relFazendas.innerHTML = '<p style="color:var(--text-muted)">Cadastre fazendas para gerar este relatório.</p>';
    } else {
        relFazendas.innerHTML = fazendas.map(f => {
            const doLocal = animais.filter(a => a.fazendaId == f.id);
            const leite = doLocal.filter(a => a.tipo === 'Leite').length;
            const corte = doLocal.filter(a => a.tipo === 'Corte').length;
            const pct = animais.length ? Math.round((doLocal.length / animais.length) * 100) : 0;
            return `
                <div class="activity-item" style="border-left: 4px solid var(--secondary);">
                    <div class="activity-details">
                        <p class="activity-title">${f.nome}</p>
                        <span class="activity-date">${leite} Leite · ${corte} Corte</span>
                    </div>
                    <span class="status-badge em-breve">${doLocal.length} (${pct}%)</span>
                </div>
            `;
        }).join('');
    }

    // 2. Situação sanitária: animais vacinados vs. sem registro
    const relSanitario = document.getElementById('relatorio-sanitario');
    if (animais.length === 0) {
        relSanitario.innerHTML = '<p style="color:var(--text-muted)">Cadastre animais para gerar este relatório.</p>';
    } else {
        const vacinados = animais.filter(a => vacinas.some(v => v.animalId == a.id));
        const semVacina = animais.filter(a => !vacinas.some(v => v.animalId == a.id));
        const pctVac = Math.round((vacinados.length / animais.length) * 100);
        const listaSem = semVacina.length
            ? semVacina.map(a => `
                <div class="activity-item" style="border-left: 4px solid var(--status-hoje);">
                    <div class="activity-details">
                        <p class="activity-title">Brinco: ${a.brinco}</p>
                        <span class="activity-date">Nenhuma vacina registrada</span>
                    </div>
                    <span class="status-badge hoje">Pendente</span>
                </div>`).join('')
            : '<p style="color:var(--text-muted)">Todos os animais possuem ao menos uma vacina. ✔</p>';
        relSanitario.innerHTML = `
            <div class="activity-item" style="border-left: 4px solid var(--primary);">
                <div class="activity-details">
                    <p class="activity-title">Cobertura Vacinal</p>
                    <span class="activity-date">${vacinados.length} de ${animais.length} animais</span>
                </div>
                <span class="status-badge" style="background-color: var(--primary);">${pctVac}%</span>
            </div>
            ${listaSem}
        `;
    }

    // 3. Composição do rebanho
    const relComposicao = document.getElementById('relatorio-composicao');
    if (animais.length === 0) {
        relComposicao.innerHTML = '<p style="color:var(--text-muted)">Cadastre animais para gerar este relatório.</p>';
    } else {
        const leite = animais.filter(a => a.tipo === 'Leite').length;
        const corte = animais.filter(a => a.tipo === 'Corte').length;
        const pctLeite = Math.round((leite / animais.length) * 100);
        const pctCorte = 100 - pctLeite;
        const mediaPorFazenda = fazendas.length ? (animais.length / fazendas.length).toFixed(1) : '—';
        relComposicao.innerHTML = `
            <div class="activity-item" style="border-left: 4px solid var(--status-breve);">
                <div class="activity-details">
                    <p class="activity-title">Vacas de Leite</p>
                    <span class="activity-date">${leite} animais</span>
                </div>
                <span class="status-badge em-breve">${pctLeite}%</span>
            </div>
            <div class="activity-item" style="border-left: 4px solid var(--status-amanha);">
                <div class="activity-details">
                    <p class="activity-title">Vacas de Corte</p>
                    <span class="activity-date">${corte} animais</span>
                </div>
                <span class="status-badge amanha">${pctCorte}%</span>
            </div>
            <div class="activity-item" style="border-left: 4px solid var(--secondary);">
                <div class="activity-details">
                    <p class="activity-title">Média por Fazenda</p>
                    <span class="activity-date">${fazendas.length} fazenda(s) ativa(s)</span>
                </div>
                <span class="status-badge" style="background-color: var(--secondary);">${mediaPorFazenda}</span>
            </div>
        `;
    }
}

// ==========================================================================
// CONTROLE DE ABERTURA E FECHAMENTO DOS MODAIS
// ==========================================================================
function abrirModalAnimal() {
    document.getElementById('form-animal').reset();
    document.getElementById('edit-animal-id').value = "";
    document.getElementById('modal-animal-titulo').innerText = "Novo Animal";
    modalAnimal.style.display = 'flex';
}

function abrirModalFazenda() {
    document.getElementById('form-fazenda').reset();
    document.getElementById('edit-fazenda-id').value = "";
    document.getElementById('modal-fazenda-titulo').innerText = "Nova Fazenda";
    modalFazenda.style.display = 'flex';
}

function abrirModalVacina() {
    if (animais.length === 0) { return alert('Cadastre um animal antes de registrar vacinas.'); }
    document.getElementById('form-vacina').reset();
    document.getElementById('edit-vacina-id').value = "";
    document.getElementById('modal-vacina-titulo').innerText = "Registrar Aplicação de Vacina";
    modalVacina.style.display = 'flex';
}

// Botões do Hero
document.getElementById('btn-add-animal').addEventListener('click', abrirModalAnimal);
document.getElementById('btn-add-fazenda').addEventListener('click', abrirModalFazenda);
document.getElementById('btn-add-vacina').addEventListener('click', abrirModalVacina);

// Botões "+" no cabeçalho dos painéis
document.querySelectorAll('[data-add]').forEach(btn => {
    btn.addEventListener('click', () => {
        if (btn.dataset.add === 'fazenda') abrirModalFazenda();
        if (btn.dataset.add === 'animal') abrirModalAnimal();
        if (btn.dataset.add === 'vacina') abrirModalVacina();
    });
});

// Fechar modais
document.getElementById('btn-fechar-animal').addEventListener('click', () => modalAnimal.style.display = 'none');
document.getElementById('btn-fechar-fazenda').addEventListener('click', () => modalFazenda.style.display = 'none');
document.getElementById('btn-fechar-vacina').addEventListener('click', () => modalVacina.style.display = 'none');

// ==========================================================================
// OPERAÇÕES DE EXCLUSÃO (DELETE)
// ==========================================================================
function deletarAnimal(id) {
    if (confirm("Deseja excluir este animal? As vacinas dele também sumirão.")) {
        animais = animais.filter(a => a.id !== id);
        vacinas = vacinas.filter(v => v.animalId !== id); // Remove vacinas órfãs
        atualizarDados();
    }
}

function deletarFazenda(id) {
    if (animais.some(a => a.fazendaId === id)) {
        return alert("Não é possível excluir uma fazenda com animais alocados.");
    }
    if (confirm("Confirmar exclusão da fazenda?")) {
        fazendas = fazendas.filter(f => f.id !== id);
        atualizarDados();
    }
}

function deletarVacina(id) {
    if (confirm("Deseja excluir o registro desta vacina?")) {
        vacinas = vacinas.filter(v => v.id !== id);
        atualizarDados();
    }
}

// ==========================================================================
// OPERAÇÕES DE PREPARAÇÃO DE EDIÇÃO (EDIT)
// ==========================================================================
function abrirEditarAnimal(id) {
    const a = animais.find(item => item.id === id);
    if (!a) return;
    document.getElementById('edit-animal-id').value = a.id;
    document.getElementById('animal-brinco').value = a.brinco;
    document.getElementById('animal-tipo').value = a.tipo;
    document.getElementById('animal-fazenda').value = a.fazendaId;
    document.getElementById('modal-animal-titulo').innerText = "Editar Animal";
    modalAnimal.style.display = 'flex';
}

function abrirEditarFazenda(id) {
    const f = fazendas.find(item => item.id === id);
    if (!f) return;
    document.getElementById('edit-fazenda-id').value = f.id;
    document.getElementById('fazenda-nome').value = f.nome;
    document.getElementById('fazenda-local').value = f.local;
    document.getElementById('modal-fazenda-titulo').innerText = "Editar Fazenda";
    modalFazenda.style.display = 'flex';
}

function abrirEditarVacina(id) {
    const v = vacinas.find(item => item.id === id);
    if (!v) return;
    document.getElementById('edit-vacina-id').value = v.id;
    document.getElementById('vacina-nome').value = v.nome;
    document.getElementById('vacina-animal').value = v.animalId;
    document.getElementById('vacina-data').value = v.data;
    document.getElementById('modal-vacina-titulo').innerText = "Editar Aplicação";
    modalVacina.style.display = 'flex';
}

// ==========================================================================
// SUBMIT DE FORMULÁRIOS (SAVE / UPDATE)
// ==========================================================================
document.getElementById('form-fazenda').addEventListener('submit', function (e) {
    e.preventDefault();
    const id = document.getElementById('edit-fazenda-id').value;
    const nome = document.getElementById('fazenda-nome').value;
    const local = document.getElementById('fazenda-local').value;
    if (id) { fazendas = fazendas.map(f => f.id == id ? { ...f, nome, local } : f); }
    else { fazendas.push({ id: Date.now(), nome, local }); }
    this.reset(); modalFazenda.style.display = 'none'; atualizarDados();
});

document.getElementById('form-animal').addEventListener('submit', function (e) {
    e.preventDefault();
    const id = document.getElementById('edit-animal-id').value;
    const brinco = document.getElementById('animal-brinco').value;
    const tipo = document.getElementById('animal-tipo').value;
    const fazendaId = parseInt(document.getElementById('animal-fazenda').value);
    if (id) { animais = animais.map(a => a.id == id ? { ...a, brinco, tipo, fazendaId } : a); }
    else { animais.push({ id: Date.now(), brinco, tipo, fazendaId }); }
    this.reset(); modalAnimal.style.display = 'none'; atualizarDados();
});

document.getElementById('form-vacina').addEventListener('submit', function (e) {
    e.preventDefault();
    const id = document.getElementById('edit-vacina-id').value;
    const nome = document.getElementById('vacina-nome').value;
    const animalId = parseInt(document.getElementById('vacina-animal').value);
    const data = document.getElementById('vacina-data').value;
    if (id) { vacinas = vacinas.map(v => v.id == id ? { ...v, nome, animalId, data } : v); }
    else { vacinas.push({ id: Date.now(), nome, animalId, data }); }
    this.reset(); modalVacina.style.display = 'none'; atualizarDados();
});

// Inicialização
window.addEventListener('DOMContentLoaded', () => {
    atualizarDados();
    mudarAba('inicio');
});
