import { Component, EventEmitter, Output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

interface ShowSections {
  escales: boolean;
  heuresDepart: boolean;
  dureeVoyage: boolean;
}

interface FilterOptions {
  escales: string[];
  heuresDepart: { min: number; max: number };
  dureeVoyage: { min: number; max: number };
}

@Component({
  selector: 'app-filter-sidebar',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './filter-sidebar.component.html',
  styleUrls: ['./filter-sidebar.component.scss']
})
export class FilterSidebarComponent {
  @Output() filtersChanged = new EventEmitter<FilterOptions>();
  showSections = signal<ShowSections>({
    escales: true,
    heuresDepart: true,
    dureeVoyage: true
  });
  filters = signal({
    direct: true,
    oneStop: true,
    multipleStops: true
  });
  departureTime = signal({
    min: 0,    // 00:00
    max: 1439  // 23:59
  });
  journeyDuration = signal({
    min: 60,  // 1.0 hours
    max: 1320 // 22.0 hours
  });
  toggleSection(section: keyof ShowSections) {
    const current = this.showSections();
    current[section] = !current[section];
    this.showSections.set({ ...current });
  }
  onDirectChange(event: Event) {
    const target = event.target as HTMLInputElement;
    this.filters.update(current => ({ ...current, direct: target.checked }));
    this.emitFilters();
  }
  onOneStopChange(event: Event) {
    const target = event.target as HTMLInputElement;
    this.filters.update(current => ({ ...current, oneStop: target.checked }));
    this.emitFilters();
  }
  onMultipleStopsChange(event: Event) {
    const target = event.target as HTMLInputElement;
    this.filters.update(current => ({ ...current, multipleStops: target.checked }));
    this.emitFilters();
  }
  onDepartureMinChange(event: Event) {
    const target = event.target as HTMLInputElement;
    this.departureTime.update(current => ({ ...current, min: parseInt(target.value) }));
    this.emitFilters();
  }
  onDepartureMaxChange(event: Event) {
    const target = event.target as HTMLInputElement;
    this.departureTime.update(current => ({ ...current, max: parseInt(target.value) }));
    this.emitFilters();
  }
  onJourneyMinChange(event: Event) {
    const target = event.target as HTMLInputElement;
    const value = Math.max(60, parseInt(target.value)); // Enforce min 1h
    this.journeyDuration.update(current => ({ ...current, min: value }));
    this.emitFilters();
  }
  onJourneyMaxChange(event: Event) {
    const target = event.target as HTMLInputElement;
    const value = Math.min(1320, parseInt(target.value)); // Enforce max 22h
    this.journeyDuration.update(current => ({ ...current, max: value }));
    this.emitFilters();
  }
  private emitFilters() {
    const escales = [];
    const filters = this.filters();
   
    if (filters.direct) escales.push('direct');
    if (filters.oneStop) escales.push('1');
    if (filters.multipleStops) escales.push('2+');
    this.filtersChanged.emit({
      escales,
      heuresDepart: this.departureTime(),
      dureeVoyage: this.journeyDuration()
    });
  }
  formatTime(minutes: number): string {
    const hours = Math.floor(minutes / 60);
    const mins = minutes % 60;
    return `${hours.toString().padStart(2, '0')}:${mins.toString().padStart(2, '0')}`;
  }
  formatDuration(minutes: number): string {
    const hours = Math.floor(minutes / 60);
    const mins = minutes % 60;
    return `${hours}h ${mins}m`;
  }
}