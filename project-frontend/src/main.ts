import { Component } from '@angular/core';
import { bootstrapApplication } from '@angular/platform-browser';
import { provideHttpClient, withFetch } from '@angular/common/http';
import { FlightSearchComponent } from './components/flight-search/flight-search.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [FlightSearchComponent],
  template: `
    <app-flight-search></app-flight-search>
  `,
})
export class App {
  name = 'Flight Management App';
}

bootstrapApplication(App, {
  providers: [
    provideHttpClient(withFetch())
  ]
});