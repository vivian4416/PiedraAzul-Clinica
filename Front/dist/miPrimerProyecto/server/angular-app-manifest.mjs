
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
    'index.csr.html': {size: 13182, hash: '0685eb68ea55ec9f73ca1a4748ed17c876b764ad72e63b37f77799d4dbd9d2d3', text: () => import('./assets-chunks/index_csr_html.mjs').then(m => m.default)},
    'index.server.html': {size: 8008, hash: 'c9aa62a7e63d142ab5f80fdba39283906da8b4412ee56f264cce81dfa4e6a47c', text: () => import('./assets-chunks/index_server_html.mjs').then(m => m.default)},
    'clientes/index.html': {size: 40266, hash: '2a0ec5b1cd6432f4e80e8e96ae78ee2bba0af4cf09f84e91512305bf7a70a77a', text: () => import('./assets-chunks/clientes_index_html.mjs').then(m => m.default)},
    'crear-cita/index.html': {size: 31944, hash: '539751e780acf63ff9eddf22c3fb30b9afdd55f451efd2f0a626e428543d57df', text: () => import('./assets-chunks/crear-cita_index_html.mjs').then(m => m.default)},
    'citas/index.html': {size: 30432, hash: 'e128e00401873a1995dc1e98993bad80c616c5d1f1db45b14623f78c50115c2f', text: () => import('./assets-chunks/citas_index_html.mjs').then(m => m.default)},
    'agendar-cita/index.html': {size: 32560, hash: 'c0c30a46f74b91f2ac099a8f18df13bcf60cf3af4428c6315b0d41e4016d67f1', text: () => import('./assets-chunks/agendar-cita_index_html.mjs').then(m => m.default)},
    'configuracion/index.html': {size: 34255, hash: 'c7e6b55c03d8487bcf97d1e21a0fbe1f8652463de50c16dde865d5682a8af90d', text: () => import('./assets-chunks/configuracion_index_html.mjs').then(m => m.default)},
    'styles-U2LAC5OC.css': {size: 9831, hash: 'SbAFj3YhhJY', text: () => import('./assets-chunks/styles-U2LAC5OC_css.mjs').then(m => m.default)}
  },
};
