
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
    'index.csr.html': {size: 13182, hash: '8ca35d365d00950076b110d6c62eca115080f80163372c9ae39abdd868157c53', text: () => import('./assets-chunks/index_csr_html.mjs').then(m => m.default)},
    'index.server.html': {size: 8008, hash: 'aeec0f12e99ef8cf2d76944c785b3394947c770548d7c685609adf338494f5dd', text: () => import('./assets-chunks/index_server_html.mjs').then(m => m.default)},
    'citas/index.html': {size: 30440, hash: 'a1d6a6f3656d92100f575bd4a412c2132799344edeef11b7b76e566efc9f1ca1', text: () => import('./assets-chunks/citas_index_html.mjs').then(m => m.default)},
    'crear-cita/index.html': {size: 30973, hash: '68da6727e5b746d03f9eb506696d38604fde42600563cf9f83f523bb94f4cfbb', text: () => import('./assets-chunks/crear-cita_index_html.mjs').then(m => m.default)},
    'configuracion/index.html': {size: 34233, hash: 'e50b08b24811daacbb9ada9a5661128beccd2f231353c837f5d0f8e5edf8f1c1', text: () => import('./assets-chunks/configuracion_index_html.mjs').then(m => m.default)},
    'agendar-cita/index.html': {size: 25349, hash: 'faf8571055375faee6779323683deb94d679723375f140ecec577c434d2b6868', text: () => import('./assets-chunks/agendar-cita_index_html.mjs').then(m => m.default)},
    'clientes/index.html': {size: 40214, hash: '252e4d327ff0494bce65909cff183b3911622d49718a24e8786c0f3fe8323f06', text: () => import('./assets-chunks/clientes_index_html.mjs').then(m => m.default)},
    'styles-U2LAC5OC.css': {size: 9831, hash: 'SbAFj3YhhJY', text: () => import('./assets-chunks/styles-U2LAC5OC_css.mjs').then(m => m.default)}
  },
};
