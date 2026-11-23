// Transforma o site em app instalável (PWA): registra o service worker e controla o botão "Instalar app".
const SITE_ONLINE = 'https://douglasvargass.github.io/HerdTech-Oficial/HerdTechSite2.0/';
const ABERTO_DO_COMPUTADOR = location.protocol === 'file:';
let pedidoInstalacao = null;

function appJaInstalado() {
  return window.matchMedia('(display-mode: standalone)').matches || window.navigator.standalone === true;
}

function mostrarBotaoInstalar(mostrar) {
  const btn = document.getElementById('btn-instalar');
  if (!btn) return;
  btn.classList.toggle('hidden', !mostrar);
  btn.classList.toggle('flex', mostrar);
}

function mostrarDica(html) {
  const dica = document.getElementById('dica-instalar');
  dica.innerHTML = html;
  dica.classList.remove('hidden');
  clearTimeout(mostrarDica.timer);
  mostrarDica.timer = setTimeout(() => dica.classList.add('hidden'), 12000);
}

async function instalarApp() {
  // Arquivo aberto direto do computador: o navegador só instala pelo endereço da internet
  if (ABERTO_DO_COMPUTADOR) {
    window.open(SITE_ONLINE, '_blank');
    return;
  }
  // Chrome, Edge e Android: janela de instalação automática
  if (pedidoInstalacao) {
    pedidoInstalacao.prompt();
    const escolha = await pedidoInstalacao.userChoice;
    pedidoInstalacao = null;
    if (escolha.outcome === 'accepted') mostrarBotaoInstalar(false);
    return;
  }
  // Sem instalação automática: explica o caminho manual de cada navegador
  if (/iphone|ipad|ipod/i.test(navigator.userAgent)) {
    mostrarDica('<b class="text-slate-800">Instalar no iPhone/iPad (Safari)</b><br>Toque em <b>Compartilhar</b> (quadrado com seta) e depois em <b>Adicionar à Tela de Início</b>.');
  } else if (/android/i.test(navigator.userAgent)) {
    mostrarDica('<b class="text-slate-800">Instalar no Android</b><br>No Chrome, toque no menu <b>⋮</b> e depois em <b>Instalar app</b> (ou <b>Adicionar à tela inicial</b>).');
  } else {
    mostrarDica('<b class="text-slate-800">Instalar no computador</b><br>Use o <b>Chrome</b> ou o <b>Edge</b> e clique no ícone de instalar na barra de endereço, ou no menu <b>⋮</b> &gt; <b>Transmitir, salvar e compartilhar</b> &gt; <b>Instalar</b>.<br><span class="text-slate-400">O Firefox não instala apps.</span>');
  }
}

// Chrome, Edge e Android avisam quando o site pode ser instalado
window.addEventListener('beforeinstallprompt', (e) => {
  e.preventDefault();
  pedidoInstalacao = e;
});

window.addEventListener('appinstalled', () => {
  pedidoInstalacao = null;
  mostrarBotaoInstalar(false);
});

// O botão aparece sempre, menos quando o site já está aberto como app instalado
if (!appJaInstalado()) mostrarBotaoInstalar(true);

// Aviso no topo quando o site foi aberto pelo arquivo (duplo clique no index.html)
if (ABERTO_DO_COMPUTADOR) {
  const aviso = document.createElement('div');
  aviso.className = 'bg-emerald-600 text-white text-xs sm:text-sm text-center px-4 py-2';
  aviso.innerHTML = 'Para instalar o HerdTech como aplicativo, abra o site pelo link: ' +
    `<a href="${SITE_ONLINE}" target="_blank" rel="noopener" class="font-bold underline break-all">${SITE_ONLINE}</a>`;
  document.body.prepend(aviso);
}

// Service worker: guarda os arquivos para o app abrir mesmo sem internet
if ('serviceWorker' in navigator && !ABERTO_DO_COMPUTADOR) {
  window.addEventListener('load', () => {
    navigator.serviceWorker.register('sw.js').catch((erro) => console.warn('Service worker não registrado:', erro));
  });
}
