import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Flight } from '../../services/flight.service';

@Component({
  selector: 'app-flight-reservation-card',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './flight-reservation-card.component.html',
  styleUrls: ['./flight-reservation-card.component.scss']
})
export class FlightReservationCardComponent {
  @Input() flight!: Flight;
  @Output() reserve = new EventEmitter<void>();

  onReserve() {
    this.reserve.emit();
  }

  formatTime(timeStr: string | undefined): string {
    if (!timeStr) return 'N/A';
    const [hours, minutes] = timeStr.split(':').map(Number);
    return `${hours < 10 ? '0' + hours : hours}:${minutes < 10 ? '0' + minutes : minutes}`;
  }

  formatDuration(minutes: number): string {
    const hours = Math.floor(minutes / 60);
    const mins = minutes % 60;
    return `${hours}h${mins > 0 ? ' ' + mins + 'm' : ''}`;
  }

  getCityCode(cityName: string): string {
    // Map city names to airport codes
    const cityCodeMap: { [key: string]: string } = {
      'lille': 'LIL',
      'paris': 'CDG',
      'lyon': 'LYS',
      'marseille': 'MRS',
      'nice': 'NCE',
      'toulouse': 'TLS',
      'strasbourg': 'SXB',
      'nantes': 'NTE',
      'bordeaux': 'BOD',
      'rennes': 'RNS'
    };

    const normalizedCity = cityName.toLowerCase().trim();
    return cityCodeMap[normalizedCity] || cityName.substring(0, 3).toUpperCase();
  }
}