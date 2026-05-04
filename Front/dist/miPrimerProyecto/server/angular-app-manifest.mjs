
export default {
  bootstrap: () => import('./main.server.mjs').then(m => m.default),
  inlineCriticalCss: true,
  baseHref: '/',
  locale: undefined,
  routes: [
  {
    "renderMode": 1,
    "route": "/"
  },
  {
    "renderMode": 1,
    "route": "/clientes"
  },
  {
    "renderMode": 1,
    "route": "/citas"
  },
  {
    "renderMode": 1,
    "route": "/crear-cita"
  },
  {
    "renderMode": 1,
    "route": "/agendar-cita"
  },
  {
    "renderMode": 1,
    "route": "/configuracion"
  },
  {
    "renderMode": 1,
    "redirectTo": "/",
    "route": "/**"
  }
],
  entryPointToBrowserMapping: undefined,
  assets: {
    'index.csr.html': {size: 13182, hash: '56928201ce0e46aab7213709cd749ec28deedee3e42e5587ec881c586becd95a', text: () => import('./assets-chunks/index_csr_html.mjs').then(m => m.default)},
    'index.server.html': {size: 8008, hash: '418a1be09b1a3a4c8eba71002da1117a81bfb94b50b9530c5268b955c7363b75', text: () => import('./assets-chunks/index_server_html.mjs').then(m => m.default)},
    'styles-U2LAC5OC.css': {size: 9831, hash: 'SbAFj3YhhJY', text: () => import('./assets-chunks/styles-U2LAC5OC_css.mjs').then(m => m.default)}
  },
};
