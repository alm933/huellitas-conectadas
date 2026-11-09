import { Component, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { SessionService } from './core/session.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  template: `
    @if (session.logoutError(); as error) {
      <p class="alert alert-danger mb-0" role="alert">{{ error }}</p>
    }
    <router-outlet />
  `
})
export class App {
  protected readonly session = inject(SessionService);
}
