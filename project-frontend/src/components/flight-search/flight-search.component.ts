import { Component, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CalendarComponent } from '../../lib/shared/calendar/calendar.component';
import { FilterSidebarComponent } from '../filter-sidebar/filter-sidebar.component';
import { FlightReservationCardComponent } from '../flight-reservation-card/flight-reservation-card.component';
import { FlightService, Flight, FlightSearchParams, FlightSearchResponse } from '../../services/flight.service';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { catchError } from 'rxjs/operators';
import { throwError } from 'rxjs';

interface FilterOptions {
  escales: string[];
  heuresDepart: { min: number; max: number };
  dureeVoyage: { min: number; max: number };
}

interface ReservationRequest {
  volId: string;
  passager: {
    nom: string;
    prenom: string;
    email: string;
  };
  nombrePlaces: number;
}

interface ReservationResponse {
  numeroReservation: string;
  volId: string;
  passager: {
    nom: string;
    prenom: string;
    email: string;
    nomComplet: string;
  };
  nombrePlaces: number;
  dateReservation: string | null;
}

@Component({
  selector: 'app-flight-search',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    CalendarComponent,
    FilterSidebarComponent,
    FlightReservationCardComponent
  ],
  templateUrl: './flight-search.component.html',
  styleUrls: ['./flight-search.component.scss']
})
export class FlightSearchComponent implements OnInit {
  searchParams = signal<FlightSearchParams>({
    villeDepart: '',
    villeArrivee: '',
    dateDepart: '',
    dateArrivee: '',
    tri: undefined
  });
  showCalendar = signal(false);
  calendarType = signal<'depart' | 'return' | null>(null);
  isLoading = signal(false);
  searchResults = signal<FlightSearchResponse | null>(null);
  errorMessage = signal<string | null>(null);
  hasSearched = signal(false);
  filters = signal<FilterOptions>({
    escales: ['direct', '1', '2+'],
    heuresDepart: { min: 0, max: 1439 },
    dureeVoyage: { min: 180, max: 1320 }
  });
  // Reservation state
  showReservationPopup = signal(false);
  selectedFlightId = signal<string | null>(null);
  reservationRequest: ReservationRequest = {
    volId: '',
    passager: { nom: '', prenom: '', email: '' },
    nombrePlaces: 1
  };
  reservationResponse: ReservationResponse | null = null;
  reservationError = signal<string | null>(null);
  reservationSuccess = signal(false);

  constructor(private flightService: FlightService, private http: HttpClient) {}

  ngOnInit() {
    // Emit initial filters
    this.applyFilters({
      escales: ['direct', '1', '2+'],
      heuresDepart: { min: 0, max: 1439 },
      dureeVoyage: { min: 60, max: 1320 }
    });
  }

  openCalendar(type: 'depart' | 'return') {
    this.calendarType.set(type);
    this.showCalendar.set(true);
  }

  getInitialDate(): Date {
    const type = this.calendarType();
    const dateStr = type === 'depart' ? this.searchParams().dateDepart : this.searchParams().dateArrivee;
    return dateStr ? new Date(dateStr) : new Date();
  }

  onDateSelected(date: string) {
    const type = this.calendarType();
    if (type === 'depart') {
      this.searchParams.update(params => ({ ...params, dateDepart: date }));
    } else if (type === 'return') {
      if (!this.searchParams().dateDepart) {
        this.errorMessage.set('Please select departure date first.');
        this.showCalendar.set(false);
        return;
      }
      const departDate = new Date(this.searchParams().dateDepart!);
      const returnDate = new Date(date);
      if (returnDate <= departDate) {
        this.errorMessage.set('Return date must be after departure date.');
        this.showCalendar.set(false);
        return;
      }
      this.searchParams.update(params => ({ ...params, dateArrivee: date }));
    }
    this.showCalendar.set(false);
  }

  applyFilters(newFilters: FilterOptions) {
    this.filters.set(newFilters);
  }

  updateTriValue(value: string) {
    this.searchParams.update(params => ({ 
      ...params, 
      tri: value === '' ? undefined : value as 'prix' | 'temps_trajet'
    }));
  }

  updateVilleDepart(value: string) {
    this.searchParams.update(params => ({ ...params, villeDepart: value }));
  }

  updateVilleArrivee(value: string) {
    this.searchParams.update(params => ({ ...params, villeArrivee: value }));
  }

  searchFlights() {
    if (!this.isSearchValid()) {
      this.errorMessage.set('Veuillez remplir tous les champs (départ, arrivée, date de départ et date de retour).');
      return;
    }
    this.isLoading.set(true);
    this.errorMessage.set(null);

    this.flightService.searchFlights(this.searchParams()).subscribe({
      next: (response) => {
        this.searchResults.set(response);
        console.log('Received flights:', response.flights);
        this.hasSearched.set(true);
        this.isLoading.set(false);
      },
      error: (error) => {
        this.errorMessage.set('Une erreur est survenue lors de la recherche. Veuillez réessayer.');
        this.isLoading.set(false);
        console.error('Search error:', error);
      }
    });
  }

  filteredFlights(): Flight[] {
    const results = this.searchResults()?.flights || [];
    const filters = this.filters();
    
    let filtered = results.filter(flight => {
      let escalesValue: string;
      if (flight.direct === true) {
        escalesValue = 'direct';
      } else if (flight.escales === 0 || flight.escales === null || flight.escales === undefined) {
        escalesValue = 'direct';
      } else if (flight.escales === 1) {
        escalesValue = '1';
      } else if (flight.escales >= 2) {
        escalesValue = '2+';
      } else {
        escalesValue = 'direct';
      }
      const departureMinutes = flight.heureDepart ? this.parseTimeToMinutes(flight.heureDepart) : 0;
      const isEscalesMatch = filters.escales.includes(escalesValue);
      const isTimeMatch = departureMinutes >= filters.heuresDepart.min && departureMinutes <= filters.heuresDepart.max;
      const isDurationMatch = flight.tempsTrajet >= filters.dureeVoyage.min && flight.tempsTrajet <= filters.dureeVoyage.max;
      return isEscalesMatch && isTimeMatch && isDurationMatch;
    });

    // Client-side sorting as fallback (API should handle this, but keeping for safety)
    const sortType = this.searchParams().tri;
    if (sortType === 'prix') {
      filtered = filtered.sort((a, b) => a.prix - b.prix);
    } else if (sortType === 'temps_trajet') {
      filtered = filtered.sort((a, b) => a.tempsTrajet - b.tempsTrajet);
    }

    return filtered;
  }

  private parseTimeToMinutes(timeStr: string): number {
    const [hours, minutes] = timeStr.split(':').map(Number);
    return hours * 60 + (minutes || 0);
  }

  formatDuration(minutes: number): string {
    const hours = Math.floor(minutes / 60);
    const mins = minutes % 60;
    return `${hours}h ${mins}m`;
  }

  isSearchValid(): boolean {
    const params = this.searchParams();
    return !!params.villeDepart && !!params.villeArrivee && !!params.dateDepart && !!params.dateArrivee;
  }

  getSearchSummary(): string {
    const params = this.searchParams();
    return `${params.villeDepart} - ${params.villeArrivee}`;
  }

  getLowestPrice(): number {
    const flights = this.filteredFlights();
    if (flights.length === 0) return 0;
    return Math.min(...flights.map(f => f.prix));
  }

  getHighestPrice(): number {
    const flights = this.filteredFlights();
    if (flights.length === 0) return 0;
    return Math.max(...flights.map(f => f.prix));
  }

  getFastestPrice(): number {
    const flights = this.filteredFlights();
    if (flights.length === 0) return 0;
    const fastest = flights.reduce((prev, current) =>
      current.tempsTrajet < prev.tempsTrajet ? current : prev
    );
    return fastest.prix;
  }

  getShortestDuration(): number {
    const flights = this.filteredFlights();
    if (flights.length === 0) return 0;
    return Math.min(...flights.map(f => f.tempsTrajet));
  }

  goBack(): void {
    this.hasSearched.set(false);
    this.searchResults.set(null);
  }

  stopPropagation(event: Event) {
    event.stopPropagation();
  }

  openReservationPopup(flightId: string) {
    this.selectedFlightId.set(flightId);
    this.reservationRequest = {
      volId: flightId,
      passager: { nom: '', prenom: '', email: '' },
      nombrePlaces: 1
    };
    this.reservationError.set(null);
    this.reservationSuccess.set(false);
    this.showReservationPopup.set(true);
  }

  closeReservationPopup() {
    this.showReservationPopup.set(false);
    this.reservationResponse = null;
    this.reservationSuccess.set(false);
    this.reservationError.set(null);
  }

  submitReservation() {
    if (!this.reservationRequest.volId || !this.reservationRequest.passager.nom ||
        !this.reservationRequest.passager.prenom || !this.reservationRequest.passager.email ||
        !this.reservationRequest.nombrePlaces) {
      this.reservationError.set('Veuillez remplir tous les champs.');
      return;
    }
    const headers = new HttpHeaders({ 'Content-Type': 'application/json' });
    this.http.post<ReservationResponse>('http://localhost:8080/api/reservations', this.reservationRequest, { headers })
      .pipe(
        catchError(error => {
          let errorMessage = 'Erreur lors de la réservation. Veuillez réessayer.';
          if (error.error && typeof error.error === 'object' && error.error.message) {
            errorMessage = error.error.message;
          } else if (error.error && typeof error.error === 'string') {
            errorMessage = error.error;
          }
          this.reservationError.set(errorMessage);
          return throwError(() => error);
        })
      )
      .subscribe(
        response => {
          this.reservationResponse = response;
          this.reservationSuccess.set(true);
          this.reservationError.set(null);
        },
        error => {
          console.error('Reservation error:', error);
        }
      );
  }

  getFlightDetails(flightId: string | null): Flight | undefined {
    const flights = this.searchResults()?.flights || [];
    return flights.find(flight => flight.id === flightId);
  }

  formatFlightDate(dateStr: string): string {
    const date = new Date(dateStr);
    return date.toLocaleDateString('fr-FR', { 
      weekday: 'short',
      day: 'numeric',
      month: 'short'
    });
  }
}