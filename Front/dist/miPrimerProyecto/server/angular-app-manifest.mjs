
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
    'index.csr.html': {size: 13182, hash: '65330bf648308749ad0c9d15d7edf1a754df8721e05fe73ade255c99adaac49b', text: () => import('./assets-chunks/index_csr_html.mjs').then(m => m.default)},
    'index.server.html': {size: 8008, hash: '69c8b3be3020dbeaf057721bef1a795caf8883453619651a56653daab3106e26', text: () => import('./assets-chunks/index_server_html.mjs').then(m => m.default)},
    'citas/index.html': {size: 30440, hash: '436cc12f4785cd2d7aa4789ef957d6d9c9c362a0e7e6261a1d25e5a5d0ed2449', text: () => import('./assets-chunks/citas_index_html.mjs').then(m => m.default)},
    'clientes/index.html': {size: 40266, hash: '0ddabbaa067718465c70dbce1b9dcf2992a9cd3052f676ddaa7558dcddef6981', text: () => import('./assets-chunks/clientes_index_html.mjs').then(m => m.default)},
    'crear-cita/index.html': {size: 31944, hash: 'b7224b739345800057246b3ae1ad70b8c401524d677308810b855f7bf7a25f4b', text: () => import('./assets-chunks/crear-cita_index_html.mjs').then(m => m.default)},
    'configuracion/index.html': {size: 34255, hash: '7a509d9eb617bfd67894fdcf002e619b71ae9d753b9408b3c11103e8acac46e4', text: () => import('./assets-chunks/configuracion_index_html.mjs').then(m => m.default)},
    'agendar-cita/index.html': {size: 32805, hash: '04586d4ef5987c45ca077737f6c1f5ca9eaaa927ce4800d9f00486ed1118e423', text: () => import('./assets-chunks/agendar-cita_index_html.mjs').then(m => m.default)},
    'styles-U2LAC5OC.css': {size: 9831, hash: 'SbAFj3YhhJY', text: () => import('./assets-chunks/styles-U2LAC5OC_css.mjs').then(m => m.default)}
  },
};
