import { Component, OnInit } from '@angular/core';
import { IonApp, IonRouterOutlet } from '@ionic/angular/standalone';
import { App } from '@capacitor/app';
import { AuthService } from './services/auth.service';
import { Router } from '@angular/router';
import { Browser } from '@capacitor/browser';

@Component({
  selector: 'app-root',
  templateUrl: 'app.component.html',
  imports: [IonApp, IonRouterOutlet],
})
export class AppComponent implements OnInit {
  constructor(private authService: AuthService, private router: Router) {
    alert('constructor corriendo');
    App.addListener('appUrlOpen', async ({ url }) => {
      alert('Deep link: ' + url);
    });
  }

  ngOnInit() {
    alert('AppComponent iniciado');

    App.addListener('appUrlOpen', async ({ url }) => {
      if (url.includes('co.piedrazul.clinica://login')) {
        await Browser.close();
        await this.authService.handleRedirectCallback(url);
        alert('isLoggedIn: ' + this.authService.isLoggedIn());
        await this.router.navigate(['/citas']);
      }
    });
  }
}
