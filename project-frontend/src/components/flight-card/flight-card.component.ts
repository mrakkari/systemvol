import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Flight } from '../../services/flight.service';

@Component({
  selector: 'app-flight-card',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './flight-card.component.html',
  styleUrls: ['./flight-card.component.scss']
})
export class FlightCardComponent {
  @Input() flight!: Flight;

  getAirportCode(city: string): string {
    const codes: { [key: string]: string } = {
      'Paris': 'ORY',
      'Lyon': 'LYS', 
      'Marseille': 'MRS',
      'Nice': 'NCE',
      'Toulouse': 'TLS',
      'Djerba': 'DJE'
    };
    return codes[city] || city.substring(0, 3).toUpperCase();
  }

  formatDuration(minutes: number): string {
    const hours = Math.floor(minutes / 60);
    const mins = minutes % 60;
    return `${hours}h ${mins}m`;
  }
}