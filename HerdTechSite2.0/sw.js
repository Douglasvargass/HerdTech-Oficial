// Service worker do site HerdTech 2.0: permite instalar e abrir sem internet.
// Ao alterar arquivos do site, aumente a VERSAO para os aparelhos baixarem a atualização.
const VERSAO = 'herdtech-site-v4';
const ARQUIVOS = [
  './', './index.html', './manifest.webmanifest', './logo.png',
  './css/style.css', './js/storage.js', './js/ui.js', './js/app.js', './js/pwa.js',
  './icons/icon-192.png', './icons/icon-512.png', './icons/favicon-32.png',
];
// Bibliotecas externas usadas pelo site (Tailwind, ícones e fonte): guardadas na primeira visita
const CDNS = ['cdn.tailwindcss.com', 'unpkg.com', 'fonts.googleapis.com', 'fonts.gstatic.com'];

self.addEventListener('install', (e) => {
  // cache: 'reload' busca sempre a versão nova no servidor (ignora o cache HTTP do navegador)
  e.waitUntil(caches.open(VERSAO)
    .then((c) => c.addAll(ARQUIVOS.map((u) => new Request(u, { cache: 'reload' }))))
    .then(() => self.skipWaiting()));
});

self.addEventListener('activate', (e) => {
  e.waitUntil((async () => {
    for (const k of await caches.keys()) if (k !== VERSAO) await caches.delete(k);
    await self.clients.claim();
  })());
});

self.addEventListener('fetch', (e) => {
  const req = e.request;
  if (req.method !== 'GET') return;
  const url = new URL(req.url);

  // Páginas: tenta a rede (versão mais nova) e usa o cache se estiver offline
  if (req.mode === 'navigate') {
    e.respondWith(fetch(req).catch(async () => (await caches.match('./index.html')) || Response.error()));
    return;
  }

  // Arquivos do site e bibliotecas externas: responde do cache na hora e atualiza em segundo plano
  if (url.origin === location.origin || CDNS.includes(url.hostname)) {
    e.respondWith((async () => {
      const cache = await caches.open(VERSAO);
      const salvo = await cache.match(req);
      const daRede = fetch(req).then((res) => {
        if (res && (res.ok || res.type === 'opaque')) cache.put(req, res.clone());
        return res;
      }).catch(() => null);
      return salvo || (await daRede) || Response.error();
    })());
  }
});
