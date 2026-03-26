# Code Citations

## License: desconocido
https://github.com/FinocchiaroGR/Proyecto-Gigis/blob/4e54dd44bd8fe9bd7eca20fcac2f9d6218371d75/codigo/models/consultasResultados.js

```
# 🔧 Problema de Ruta de Importación

El issue es que la ruta relativa `../../services/citas.service` es **incorrecta** desde la ubicación de `citas.ts`.

## Estructura del Proyecto:
```
src/app/
├── citas/
│   └── citas.ts          ← archivo actual
├── services/
│   └── citas.service.ts  ← archivo destino
└── models/
    └── cita.model.ts
```

Desde `citas.ts` (en `src/app/citas/`), la ruta correcta es `../services/citas.service` (una carpeta hacia arriba, no dos).

---

## ✅ Archivo Corregido:

````typescript
// filepath: c:\Users\juans\Desktop\Clases\PiedraAzul\Frontend\src\app\citas\citas.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CitasService } from '../services/citas.service';
import { Cita } from '../models/cita.model';

@Component({
  selector: 'app-citas',
  imports: [CommonModule, FormsModule],
  templateUrl: './citas.html',
  styleUrl: './citas.css',
})
export class CitasComponent implements OnInit {
  citas: Cita[] = [];
  citasFiltradas: Cita[] = [];
  
  medicosOptions = [
    { id: 0, nombre: 'Todos' },
    { id: 1, nombre: 'Dr. Andrés Mora — Quiro.' },
    { id: 2, nombre: 'Dra. Carolina Ríos — Fisio.' },
    { id: 3, nombre: 'Dr. Luis Pérez — T.Neural' },
  ];

  filtroMedico: number = 0;
  filtroFecha: string = '';
  
  estadisticas = {
    total: 0,
    confirmadas: 0,
    atendidas: 0,
    canceladas: 0,
  };

  constructor(private citasService: CitasService) {}

  ngOnInit(): void {
    this.citas = this.citasService.getCitas();
    this.citasFiltradas = this.citas.filter(c => c.medico === 1);
    this.actualizarEstadisticas();
  }

  filtrar(): void {
    const lista = this.filtroMedico 
      ? this.citas.filter(c => c.medico === this.filtroMedico) 
      : this.citas;
    this.citasFiltradas = lista;
    this.actualizarEstadisticas();
  }

  actualizarEstadisticas(): void {
    this.estadisticas = {
      total: this.citasFiltradas.length,
      confirmadas: this.citasFiltradas.filter(c => c.estado === 'CONFIRMADA').length,
      atendidas: this.citasFiltradas.filter(c => c.estado === 'ATENDIDA').length,
      canceladas: this.citasFiltradas.filter(c => c.estado === 'CANCELADA').length,
    };
  }

  formatFecha(fecha: string): string {
    if (!fecha) return '';
    const [y, m, day] = fecha.split('-');
    const meses = ['', 'Ene', 'Feb', 'Mar', 'Abr', 'May', 'Jun', 'Jul', 'Ago', 'Sep', 'Oct', 'Nov', 'Dic'];
    return `${parseInt(day)}
```


## License: desconocido
https://github.com/FinocchiaroGR/Proyecto-Gigis/blob/4e54dd44bd8fe9bd7eca20fcac2f9d6218371d75/codigo/models/consultasResultados.js

```
# 🔧 Problema de Ruta de Importación

El issue es que la ruta relativa `../../services/citas.service` es **incorrecta** desde la ubicación de `citas.ts`.

## Estructura del Proyecto:
```
src/app/
├── citas/
│   └── citas.ts          ← archivo actual
├── services/
│   └── citas.service.ts  ← archivo destino
└── models/
    └── cita.model.ts
```

Desde `citas.ts` (en `src/app/citas/`), la ruta correcta es `../services/citas.service` (una carpeta hacia arriba, no dos).

---

## ✅ Archivo Corregido:

````typescript
// filepath: c:\Users\juans\Desktop\Clases\PiedraAzul\Frontend\src\app\citas\citas.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CitasService } from '../services/citas.service';
import { Cita } from '../models/cita.model';

@Component({
  selector: 'app-citas',
  imports: [CommonModule, FormsModule],
  templateUrl: './citas.html',
  styleUrl: './citas.css',
})
export class CitasComponent implements OnInit {
  citas: Cita[] = [];
  citasFiltradas: Cita[] = [];
  
  medicosOptions = [
    { id: 0, nombre: 'Todos' },
    { id: 1, nombre: 'Dr. Andrés Mora — Quiro.' },
    { id: 2, nombre: 'Dra. Carolina Ríos — Fisio.' },
    { id: 3, nombre: 'Dr. Luis Pérez — T.Neural' },
  ];

  filtroMedico: number = 0;
  filtroFecha: string = '';
  
  estadisticas = {
    total: 0,
    confirmadas: 0,
    atendidas: 0,
    canceladas: 0,
  };

  constructor(private citasService: CitasService) {}

  ngOnInit(): void {
    this.citas = this.citasService.getCitas();
    this.citasFiltradas = this.citas.filter(c => c.medico === 1);
    this.actualizarEstadisticas();
  }

  filtrar(): void {
    const lista = this.filtroMedico 
      ? this.citas.filter(c => c.medico === this.filtroMedico) 
      : this.citas;
    this.citasFiltradas = lista;
    this.actualizarEstadisticas();
  }

  actualizarEstadisticas(): void {
    this.estadisticas = {
      total: this.citasFiltradas.length,
      confirmadas: this.citasFiltradas.filter(c => c.estado === 'CONFIRMADA').length,
      atendidas: this.citasFiltradas.filter(c => c.estado === 'ATENDIDA').length,
      canceladas: this.citasFiltradas.filter(c => c.estado === 'CANCELADA').length,
    };
  }

  formatFecha(fecha: string): string {
    if (!fecha) return '';
    const [y, m, day] = fecha.split('-');
    const meses = ['', 'Ene', 'Feb', 'Mar', 'Abr', 'May', 'Jun', 'Jul', 'Ago', 'Sep', 'Oct', 'Nov', 'Dic'];
    return `${parseInt(day)}
```


## License: desconocido
https://github.com/FinocchiaroGR/Proyecto-Gigis/blob/4e54dd44bd8fe9bd7eca20fcac2f9d6218371d75/codigo/models/consultasResultados.js

```
# 🔧 Problema de Ruta de Importación

El issue es que la ruta relativa `../../services/citas.service` es **incorrecta** desde la ubicación de `citas.ts`.

## Estructura del Proyecto:
```
src/app/
├── citas/
│   └── citas.ts          ← archivo actual
├── services/
│   └── citas.service.ts  ← archivo destino
└── models/
    └── cita.model.ts
```

Desde `citas.ts` (en `src/app/citas/`), la ruta correcta es `../services/citas.service` (una carpeta hacia arriba, no dos).

---

## ✅ Archivo Corregido:

````typescript
// filepath: c:\Users\juans\Desktop\Clases\PiedraAzul\Frontend\src\app\citas\citas.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CitasService } from '../services/citas.service';
import { Cita } from '../models/cita.model';

@Component({
  selector: 'app-citas',
  imports: [CommonModule, FormsModule],
  templateUrl: './citas.html',
  styleUrl: './citas.css',
})
export class CitasComponent implements OnInit {
  citas: Cita[] = [];
  citasFiltradas: Cita[] = [];
  
  medicosOptions = [
    { id: 0, nombre: 'Todos' },
    { id: 1, nombre: 'Dr. Andrés Mora — Quiro.' },
    { id: 2, nombre: 'Dra. Carolina Ríos — Fisio.' },
    { id: 3, nombre: 'Dr. Luis Pérez — T.Neural' },
  ];

  filtroMedico: number = 0;
  filtroFecha: string = '';
  
  estadisticas = {
    total: 0,
    confirmadas: 0,
    atendidas: 0,
    canceladas: 0,
  };

  constructor(private citasService: CitasService) {}

  ngOnInit(): void {
    this.citas = this.citasService.getCitas();
    this.citasFiltradas = this.citas.filter(c => c.medico === 1);
    this.actualizarEstadisticas();
  }

  filtrar(): void {
    const lista = this.filtroMedico 
      ? this.citas.filter(c => c.medico === this.filtroMedico) 
      : this.citas;
    this.citasFiltradas = lista;
    this.actualizarEstadisticas();
  }

  actualizarEstadisticas(): void {
    this.estadisticas = {
      total: this.citasFiltradas.length,
      confirmadas: this.citasFiltradas.filter(c => c.estado === 'CONFIRMADA').length,
      atendidas: this.citasFiltradas.filter(c => c.estado === 'ATENDIDA').length,
      canceladas: this.citasFiltradas.filter(c => c.estado === 'CANCELADA').length,
    };
  }

  formatFecha(fecha: string): string {
    if (!fecha) return '';
    const [y, m, day] = fecha.split('-');
    const meses = ['', 'Ene', 'Feb', 'Mar', 'Abr', 'May', 'Jun', 'Jul', 'Ago', 'Sep', 'Oct', 'Nov', 'Dic'];
    return `${parseInt(day)}
```


## License: desconocido
https://github.com/FinocchiaroGR/Proyecto-Gigis/blob/4e54dd44bd8fe9bd7eca20fcac2f9d6218371d75/codigo/models/consultasResultados.js

```
# 🔧 Problema de Ruta de Importación

El issue es que la ruta relativa `../../services/citas.service` es **incorrecta** desde la ubicación de `citas.ts`.

## Estructura del Proyecto:
```
src/app/
├── citas/
│   └── citas.ts          ← archivo actual
├── services/
│   └── citas.service.ts  ← archivo destino
└── models/
    └── cita.model.ts
```

Desde `citas.ts` (en `src/app/citas/`), la ruta correcta es `../services/citas.service` (una carpeta hacia arriba, no dos).

---

## ✅ Archivo Corregido:

````typescript
// filepath: c:\Users\juans\Desktop\Clases\PiedraAzul\Frontend\src\app\citas\citas.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CitasService } from '../services/citas.service';
import { Cita } from '../models/cita.model';

@Component({
  selector: 'app-citas',
  imports: [CommonModule, FormsModule],
  templateUrl: './citas.html',
  styleUrl: './citas.css',
})
export class CitasComponent implements OnInit {
  citas: Cita[] = [];
  citasFiltradas: Cita[] = [];
  
  medicosOptions = [
    { id: 0, nombre: 'Todos' },
    { id: 1, nombre: 'Dr. Andrés Mora — Quiro.' },
    { id: 2, nombre: 'Dra. Carolina Ríos — Fisio.' },
    { id: 3, nombre: 'Dr. Luis Pérez — T.Neural' },
  ];

  filtroMedico: number = 0;
  filtroFecha: string = '';
  
  estadisticas = {
    total: 0,
    confirmadas: 0,
    atendidas: 0,
    canceladas: 0,
  };

  constructor(private citasService: CitasService) {}

  ngOnInit(): void {
    this.citas = this.citasService.getCitas();
    this.citasFiltradas = this.citas.filter(c => c.medico === 1);
    this.actualizarEstadisticas();
  }

  filtrar(): void {
    const lista = this.filtroMedico 
      ? this.citas.filter(c => c.medico === this.filtroMedico) 
      : this.citas;
    this.citasFiltradas = lista;
    this.actualizarEstadisticas();
  }

  actualizarEstadisticas(): void {
    this.estadisticas = {
      total: this.citasFiltradas.length,
      confirmadas: this.citasFiltradas.filter(c => c.estado === 'CONFIRMADA').length,
      atendidas: this.citasFiltradas.filter(c => c.estado === 'ATENDIDA').length,
      canceladas: this.citasFiltradas.filter(c => c.estado === 'CANCELADA').length,
    };
  }

  formatFecha(fecha: string): string {
    if (!fecha) return '';
    const [y, m, day] = fecha.split('-');
    const meses = ['', 'Ene', 'Feb', 'Mar', 'Abr', 'May', 'Jun', 'Jul', 'Ago', 'Sep', 'Oct', 'Nov', 'Dic'];
    return `${parseInt(day)}
```


## License: desconocido
https://github.com/FinocchiaroGR/Proyecto-Gigis/blob/4e54dd44bd8fe9bd7eca20fcac2f9d6218371d75/codigo/models/consultasResultados.js

```
# 🔧 Problema de Ruta de Importación

El issue es que la ruta relativa `../../services/citas.service` es **incorrecta** desde la ubicación de `citas.ts`.

## Estructura del Proyecto:
```
src/app/
├── citas/
│   └── citas.ts          ← archivo actual
├── services/
│   └── citas.service.ts  ← archivo destino
└── models/
    └── cita.model.ts
```

Desde `citas.ts` (en `src/app/citas/`), la ruta correcta es `../services/citas.service` (una carpeta hacia arriba, no dos).

---

## ✅ Archivo Corregido:

````typescript
// filepath: c:\Users\juans\Desktop\Clases\PiedraAzul\Frontend\src\app\citas\citas.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CitasService } from '../services/citas.service';
import { Cita } from '../models/cita.model';

@Component({
  selector: 'app-citas',
  imports: [CommonModule, FormsModule],
  templateUrl: './citas.html',
  styleUrl: './citas.css',
})
export class CitasComponent implements OnInit {
  citas: Cita[] = [];
  citasFiltradas: Cita[] = [];
  
  medicosOptions = [
    { id: 0, nombre: 'Todos' },
    { id: 1, nombre: 'Dr. Andrés Mora — Quiro.' },
    { id: 2, nombre: 'Dra. Carolina Ríos — Fisio.' },
    { id: 3, nombre: 'Dr. Luis Pérez — T.Neural' },
  ];

  filtroMedico: number = 0;
  filtroFecha: string = '';
  
  estadisticas = {
    total: 0,
    confirmadas: 0,
    atendidas: 0,
    canceladas: 0,
  };

  constructor(private citasService: CitasService) {}

  ngOnInit(): void {
    this.citas = this.citasService.getCitas();
    this.citasFiltradas = this.citas.filter(c => c.medico === 1);
    this.actualizarEstadisticas();
  }

  filtrar(): void {
    const lista = this.filtroMedico 
      ? this.citas.filter(c => c.medico === this.filtroMedico) 
      : this.citas;
    this.citasFiltradas = lista;
    this.actualizarEstadisticas();
  }

  actualizarEstadisticas(): void {
    this.estadisticas = {
      total: this.citasFiltradas.length,
      confirmadas: this.citasFiltradas.filter(c => c.estado === 'CONFIRMADA').length,
      atendidas: this.citasFiltradas.filter(c => c.estado === 'ATENDIDA').length,
      canceladas: this.citasFiltradas.filter(c => c.estado === 'CANCELADA').length,
    };
  }

  formatFecha(fecha: string): string {
    if (!fecha) return '';
    const [y, m, day] = fecha.split('-');
    const meses = ['', 'Ene', 'Feb', 'Mar', 'Abr', 'May', 'Jun', 'Jul', 'Ago', 'Sep', 'Oct', 'Nov', 'Dic'];
    return `${parseInt(day)}
```


## License: desconocido
https://github.com/FinocchiaroGR/Proyecto-Gigis/blob/4e54dd44bd8fe9bd7eca20fcac2f9d6218371d75/codigo/models/consultasResultados.js

```
# 🔧 Problema de Ruta de Importación

El issue es que la ruta relativa `../../services/citas.service` es **incorrecta** desde la ubicación de `citas.ts`.

## Estructura del Proyecto:
```
src/app/
├── citas/
│   └── citas.ts          ← archivo actual
├── services/
│   └── citas.service.ts  ← archivo destino
└── models/
    └── cita.model.ts
```

Desde `citas.ts` (en `src/app/citas/`), la ruta correcta es `../services/citas.service` (una carpeta hacia arriba, no dos).

---

## ✅ Archivo Corregido:

````typescript
// filepath: c:\Users\juans\Desktop\Clases\PiedraAzul\Frontend\src\app\citas\citas.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CitasService } from '../services/citas.service';
import { Cita } from '../models/cita.model';

@Component({
  selector: 'app-citas',
  imports: [CommonModule, FormsModule],
  templateUrl: './citas.html',
  styleUrl: './citas.css',
})
export class CitasComponent implements OnInit {
  citas: Cita[] = [];
  citasFiltradas: Cita[] = [];
  
  medicosOptions = [
    { id: 0, nombre: 'Todos' },
    { id: 1, nombre: 'Dr. Andrés Mora — Quiro.' },
    { id: 2, nombre: 'Dra. Carolina Ríos — Fisio.' },
    { id: 3, nombre: 'Dr. Luis Pérez — T.Neural' },
  ];

  filtroMedico: number = 0;
  filtroFecha: string = '';
  
  estadisticas = {
    total: 0,
    confirmadas: 0,
    atendidas: 0,
    canceladas: 0,
  };

  constructor(private citasService: CitasService) {}

  ngOnInit(): void {
    this.citas = this.citasService.getCitas();
    this.citasFiltradas = this.citas.filter(c => c.medico === 1);
    this.actualizarEstadisticas();
  }

  filtrar(): void {
    const lista = this.filtroMedico 
      ? this.citas.filter(c => c.medico === this.filtroMedico) 
      : this.citas;
    this.citasFiltradas = lista;
    this.actualizarEstadisticas();
  }

  actualizarEstadisticas(): void {
    this.estadisticas = {
      total: this.citasFiltradas.length,
      confirmadas: this.citasFiltradas.filter(c => c.estado === 'CONFIRMADA').length,
      atendidas: this.citasFiltradas.filter(c => c.estado === 'ATENDIDA').length,
      canceladas: this.citasFiltradas.filter(c => c.estado === 'CANCELADA').length,
    };
  }

  formatFecha(fecha: string): string {
    if (!fecha) return '';
    const [y, m, day] = fecha.split('-');
    const meses = ['', 'Ene', 'Feb', 'Mar', 'Abr', 'May', 'Jun', 'Jul', 'Ago', 'Sep', 'Oct', 'Nov', 'Dic'];
    return `${parseInt(day)}
```


## License: desconocido
https://github.com/FinocchiaroGR/Proyecto-Gigis/blob/4e54dd44bd8fe9bd7eca20fcac2f9d6218371d75/codigo/models/consultasResultados.js

```
# 🔧 Problema de Ruta de Importación

El issue es que la ruta relativa `../../services/citas.service` es **incorrecta** desde la ubicación de `citas.ts`.

## Estructura del Proyecto:
```
src/app/
├── citas/
│   └── citas.ts          ← archivo actual
├── services/
│   └── citas.service.ts  ← archivo destino
└── models/
    └── cita.model.ts
```

Desde `citas.ts` (en `src/app/citas/`), la ruta correcta es `../services/citas.service` (una carpeta hacia arriba, no dos).

---

## ✅ Archivo Corregido:

````typescript
// filepath: c:\Users\juans\Desktop\Clases\PiedraAzul\Frontend\src\app\citas\citas.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CitasService } from '../services/citas.service';
import { Cita } from '../models/cita.model';

@Component({
  selector: 'app-citas',
  imports: [CommonModule, FormsModule],
  templateUrl: './citas.html',
  styleUrl: './citas.css',
})
export class CitasComponent implements OnInit {
  citas: Cita[] = [];
  citasFiltradas: Cita[] = [];
  
  medicosOptions = [
    { id: 0, nombre: 'Todos' },
    { id: 1, nombre: 'Dr. Andrés Mora — Quiro.' },
    { id: 2, nombre: 'Dra. Carolina Ríos — Fisio.' },
    { id: 3, nombre: 'Dr. Luis Pérez — T.Neural' },
  ];

  filtroMedico: number = 0;
  filtroFecha: string = '';
  
  estadisticas = {
    total: 0,
    confirmadas: 0,
    atendidas: 0,
    canceladas: 0,
  };

  constructor(private citasService: CitasService) {}

  ngOnInit(): void {
    this.citas = this.citasService.getCitas();
    this.citasFiltradas = this.citas.filter(c => c.medico === 1);
    this.actualizarEstadisticas();
  }

  filtrar(): void {
    const lista = this.filtroMedico 
      ? this.citas.filter(c => c.medico === this.filtroMedico) 
      : this.citas;
    this.citasFiltradas = lista;
    this.actualizarEstadisticas();
  }

  actualizarEstadisticas(): void {
    this.estadisticas = {
      total: this.citasFiltradas.length,
      confirmadas: this.citasFiltradas.filter(c => c.estado === 'CONFIRMADA').length,
      atendidas: this.citasFiltradas.filter(c => c.estado === 'ATENDIDA').length,
      canceladas: this.citasFiltradas.filter(c => c.estado === 'CANCELADA').length,
    };
  }

  formatFecha(fecha: string): string {
    if (!fecha) return '';
    const [y, m, day] = fecha.split('-');
    const meses = ['', 'Ene', 'Feb', 'Mar', 'Abr', 'May', 'Jun', 'Jul', 'Ago', 'Sep', 'Oct', 'Nov', 'Dic'];
    return `${parseInt(day)}
```


## License: desconocido
https://github.com/FinocchiaroGR/Proyecto-Gigis/blob/4e54dd44bd8fe9bd7eca20fcac2f9d6218371d75/codigo/models/consultasResultados.js

```
# 🔧 Problema de Ruta de Importación

El issue es que la ruta relativa `../../services/citas.service` es **incorrecta** desde la ubicación de `citas.ts`.

## Estructura del Proyecto:
```
src/app/
├── citas/
│   └── citas.ts          ← archivo actual
├── services/
│   └── citas.service.ts  ← archivo destino
└── models/
    └── cita.model.ts
```

Desde `citas.ts` (en `src/app/citas/`), la ruta correcta es `../services/citas.service` (una carpeta hacia arriba, no dos).

---

## ✅ Archivo Corregido:

````typescript
// filepath: c:\Users\juans\Desktop\Clases\PiedraAzul\Frontend\src\app\citas\citas.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CitasService } from '../services/citas.service';
import { Cita } from '../models/cita.model';

@Component({
  selector: 'app-citas',
  imports: [CommonModule, FormsModule],
  templateUrl: './citas.html',
  styleUrl: './citas.css',
})
export class CitasComponent implements OnInit {
  citas: Cita[] = [];
  citasFiltradas: Cita[] = [];
  
  medicosOptions = [
    { id: 0, nombre: 'Todos' },
    { id: 1, nombre: 'Dr. Andrés Mora — Quiro.' },
    { id: 2, nombre: 'Dra. Carolina Ríos — Fisio.' },
    { id: 3, nombre: 'Dr. Luis Pérez — T.Neural' },
  ];

  filtroMedico: number = 0;
  filtroFecha: string = '';
  
  estadisticas = {
    total: 0,
    confirmadas: 0,
    atendidas: 0,
    canceladas: 0,
  };

  constructor(private citasService: CitasService) {}

  ngOnInit(): void {
    this.citas = this.citasService.getCitas();
    this.citasFiltradas = this.citas.filter(c => c.medico === 1);
    this.actualizarEstadisticas();
  }

  filtrar(): void {
    const lista = this.filtroMedico 
      ? this.citas.filter(c => c.medico === this.filtroMedico) 
      : this.citas;
    this.citasFiltradas = lista;
    this.actualizarEstadisticas();
  }

  actualizarEstadisticas(): void {
    this.estadisticas = {
      total: this.citasFiltradas.length,
      confirmadas: this.citasFiltradas.filter(c => c.estado === 'CONFIRMADA').length,
      atendidas: this.citasFiltradas.filter(c => c.estado === 'ATENDIDA').length,
      canceladas: this.citasFiltradas.filter(c => c.estado === 'CANCELADA').length,
    };
  }

  formatFecha(fecha: string): string {
    if (!fecha) return '';
    const [y, m, day] = fecha.split('-');
    const meses = ['', 'Ene', 'Feb', 'Mar', 'Abr', 'May', 'Jun', 'Jul', 'Ago', 'Sep', 'Oct', 'Nov', 'Dic'];
    return `${parseInt(day)}
```


## License: desconocido
https://github.com/FinocchiaroGR/Proyecto-Gigis/blob/4e54dd44bd8fe9bd7eca20fcac2f9d6218371d75/codigo/models/consultasResultados.js

```
# 🔧 Problema de Ruta de Importación

El issue es que la ruta relativa `../../services/citas.service` es **incorrecta** desde la ubicación de `citas.ts`.

## Estructura del Proyecto:
```
src/app/
├── citas/
│   └── citas.ts          ← archivo actual
├── services/
│   └── citas.service.ts  ← archivo destino
└── models/
    └── cita.model.ts
```

Desde `citas.ts` (en `src/app/citas/`), la ruta correcta es `../services/citas.service` (una carpeta hacia arriba, no dos).

---

## ✅ Archivo Corregido:

````typescript
// filepath: c:\Users\juans\Desktop\Clases\PiedraAzul\Frontend\src\app\citas\citas.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CitasService } from '../services/citas.service';
import { Cita } from '../models/cita.model';

@Component({
  selector: 'app-citas',
  imports: [CommonModule, FormsModule],
  templateUrl: './citas.html',
  styleUrl: './citas.css',
})
export class CitasComponent implements OnInit {
  citas: Cita[] = [];
  citasFiltradas: Cita[] = [];
  
  medicosOptions = [
    { id: 0, nombre: 'Todos' },
    { id: 1, nombre: 'Dr. Andrés Mora — Quiro.' },
    { id: 2, nombre: 'Dra. Carolina Ríos — Fisio.' },
    { id: 3, nombre: 'Dr. Luis Pérez — T.Neural' },
  ];

  filtroMedico: number = 0;
  filtroFecha: string = '';
  
  estadisticas = {
    total: 0,
    confirmadas: 0,
    atendidas: 0,
    canceladas: 0,
  };

  constructor(private citasService: CitasService) {}

  ngOnInit(): void {
    this.citas = this.citasService.getCitas();
    this.citasFiltradas = this.citas.filter(c => c.medico === 1);
    this.actualizarEstadisticas();
  }

  filtrar(): void {
    const lista = this.filtroMedico 
      ? this.citas.filter(c => c.medico === this.filtroMedico) 
      : this.citas;
    this.citasFiltradas = lista;
    this.actualizarEstadisticas();
  }

  actualizarEstadisticas(): void {
    this.estadisticas = {
      total: this.citasFiltradas.length,
      confirmadas: this.citasFiltradas.filter(c => c.estado === 'CONFIRMADA').length,
      atendidas: this.citasFiltradas.filter(c => c.estado === 'ATENDIDA').length,
      canceladas: this.citasFiltradas.filter(c => c.estado === 'CANCELADA').length,
    };
  }

  formatFecha(fecha: string): string {
    if (!fecha) return '';
    const [y, m, day] = fecha.split('-');
    const meses = ['', 'Ene', 'Feb', 'Mar', 'Abr', 'May', 'Jun', 'Jul', 'Ago', 'Sep', 'Oct', 'Nov', 'Dic'];
    return `${parseInt(day)}
```


## License: desconocido
https://github.com/FinocchiaroGR/Proyecto-Gigis/blob/4e54dd44bd8fe9bd7eca20fcac2f9d6218371d75/codigo/models/consultasResultados.js

```
# 🔧 Problema de Ruta de Importación

El issue es que la ruta relativa `../../services/citas.service` es **incorrecta** desde la ubicación de `citas.ts`.

## Estructura del Proyecto:
```
src/app/
├── citas/
│   └── citas.ts          ← archivo actual
├── services/
│   └── citas.service.ts  ← archivo destino
└── models/
    └── cita.model.ts
```

Desde `citas.ts` (en `src/app/citas/`), la ruta correcta es `../services/citas.service` (una carpeta hacia arriba, no dos).

---

## ✅ Archivo Corregido:

````typescript
// filepath: c:\Users\juans\Desktop\Clases\PiedraAzul\Frontend\src\app\citas\citas.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CitasService } from '../services/citas.service';
import { Cita } from '../models/cita.model';

@Component({
  selector: 'app-citas',
  imports: [CommonModule, FormsModule],
  templateUrl: './citas.html',
  styleUrl: './citas.css',
})
export class CitasComponent implements OnInit {
  citas: Cita[] = [];
  citasFiltradas: Cita[] = [];
  
  medicosOptions = [
    { id: 0, nombre: 'Todos' },
    { id: 1, nombre: 'Dr. Andrés Mora — Quiro.' },
    { id: 2, nombre: 'Dra. Carolina Ríos — Fisio.' },
    { id: 3, nombre: 'Dr. Luis Pérez — T.Neural' },
  ];

  filtroMedico: number = 0;
  filtroFecha: string = '';
  
  estadisticas = {
    total: 0,
    confirmadas: 0,
    atendidas: 0,
    canceladas: 0,
  };

  constructor(private citasService: CitasService) {}

  ngOnInit(): void {
    this.citas = this.citasService.getCitas();
    this.citasFiltradas = this.citas.filter(c => c.medico === 1);
    this.actualizarEstadisticas();
  }

  filtrar(): void {
    const lista = this.filtroMedico 
      ? this.citas.filter(c => c.medico === this.filtroMedico) 
      : this.citas;
    this.citasFiltradas = lista;
    this.actualizarEstadisticas();
  }

  actualizarEstadisticas(): void {
    this.estadisticas = {
      total: this.citasFiltradas.length,
      confirmadas: this.citasFiltradas.filter(c => c.estado === 'CONFIRMADA').length,
      atendidas: this.citasFiltradas.filter(c => c.estado === 'ATENDIDA').length,
      canceladas: this.citasFiltradas.filter(c => c.estado === 'CANCELADA').length,
    };
  }

  formatFecha(fecha: string): string {
    if (!fecha) return '';
    const [y, m, day] = fecha.split('-');
    const meses = ['', 'Ene', 'Feb', 'Mar', 'Abr', 'May', 'Jun', 'Jul', 'Ago', 'Sep', 'Oct', 'Nov', 'Dic'];
    return `${parseInt(day)}
```

