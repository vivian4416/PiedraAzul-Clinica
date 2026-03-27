
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
    'index.csr.html': {size: 13182, hash: '2f92a6262ee98bfab73e5848ef21e4b1a63eb8b60895fb3ecc93741555654332', text: () => import('./assets-chunks/index_csr_html.mjs').then(m => m.default)},
    'index.server.html': {size: 8008, hash: '9a831057c34eb6fe38a0d1381933e8b84d0cd61bc7a6c89ae3edd757fe51213f', text: () => import('./assets-chunks/index_server_html.mjs').then(m => m.default)},
    'citas/index.html': {size: 30432, hash: '1e519f990052fd4de345a9f688e5c445ecc6b9225a6a8591072126267dab62a9', text: () => import('./assets-chunks/citas_index_html.mjs').then(m => m.default)},
    'clientes/index.html': {size: 40266, hash: '9c7caefb6f45dd7bb689db6b8b8fb9eb5df27b61f7e654350b8ff810b293a434', text: () => import('./assets-chunks/clientes_index_html.mjs').then(m => m.default)},
    'crear-cita/index.html': {size: 31961, hash: '61a29317e460c6c90a3582d37dd23df4f9ada2fcd3f8456da28b55326d54cf4d', text: () => import('./assets-chunks/crear-cita_index_html.mjs').then(m => m.default)},
    'agendar-cita/index.html': {size: 32717, hash: '622251121f343bf2452dcfacac49affbefe9fcd730d25e7efee5f5194cbfd88b', text: () => import('./assets-chunks/agendar-cita_index_html.mjs').then(m => m.default)},
    'configuracion/index.html': {size: 28578, hash: 'fb3569982f7ebc35ad1de904a1eb57dbcd8fda723e970fb819558248220aea0e', text: () => import('./assets-chunks/configuracion_index_html.mjs').then(m => m.default)},
    'styles-U2LAC5OC.css': {size: 9831, hash: 'SbAFj3YhhJY', text: () => import('./assets-chunks/styles-U2LAC5OC_css.mjs').then(m => m.default)}
  },
};
