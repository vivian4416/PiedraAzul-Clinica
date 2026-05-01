
export default {
  bootstrap: () => import('./main.server.mjs').then(m => m.default),
  inlineCriticalCss: true,
  baseHref: '/',
  locale: undefined,
  routes: [
  {
    "renderMode": 2,
    "redirectTo": "/citas",
    "route": "/"
  },
  {
    "renderMode": 2,
    "route": "/clientes"
  },
  {
    "renderMode": 2,
    "route": "/citas"
  },
  {
    "renderMode": 2,
    "route": "/crear-cita"
  },
  {
    "renderMode": 2,
    "route": "/agendar-cita"
  },
  {
    "renderMode": 2,
    "route": "/configuracion"
  }
],
  entryPointToBrowserMapping: undefined,
  assets: {
    'index.csr.html': {size: 13182, hash: '0fb07d71a4da700fcd52d805bc8de9e929235c5ff3757df4818600c92f1cf76e', text: () => import('./assets-chunks/index_csr_html.mjs').then(m => m.default)},
    'index.server.html': {size: 8008, hash: '868e07764aaa18c451649c9237e4ea30f2a9ba35fde45ec1c15f5a4fdf31dd59', text: () => import('./assets-chunks/index_server_html.mjs').then(m => m.default)},
    'clientes/index.html': {size: 40266, hash: '1f9055cf32c15aec008da0e876856de3f703944af5733f6ed9cee10a79fbb081', text: () => import('./assets-chunks/clientes_index_html.mjs').then(m => m.default)},
    'citas/index.html': {size: 30431, hash: '602a636893fd5fd2130ac5aebe4c049dc84f3c96918ed26d1dc010721bf5d58e', text: () => import('./assets-chunks/citas_index_html.mjs').then(m => m.default)},
    'configuracion/index.html': {size: 28578, hash: '2eb4101fedce5522d47c99fbdf12513ca923829da45cb6d567176e53c257669f', text: () => import('./assets-chunks/configuracion_index_html.mjs').then(m => m.default)},
    'crear-cita/index.html': {size: 31961, hash: '078d1ca345d8e488a02363c2f473e6b2289ed69c7cc997d0c249b194d1c7dd04', text: () => import('./assets-chunks/crear-cita_index_html.mjs').then(m => m.default)},
    'agendar-cita/index.html': {size: 32452, hash: 'b86f703596aad72e56cb42fd2006107e5c873323659e69f2a926be04ae3d6957', text: () => import('./assets-chunks/agendar-cita_index_html.mjs').then(m => m.default)},
    'styles-U2LAC5OC.css': {size: 9831, hash: 'SbAFj3YhhJY', text: () => import('./assets-chunks/styles-U2LAC5OC_css.mjs').then(m => m.default)}
  },
};
