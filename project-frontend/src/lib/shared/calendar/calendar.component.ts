import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-calendar',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './calendar.component.html',
  styleUrls: ['./calendar.component.scss']
})
export class CalendarComponent implements OnInit {
  @Input() initialDate: Date = new Date();
  @Output() dateSelected = new EventEmitter<string>();
  @Output() cancelled = new EventEmitter<void>();

  currentDate: Date = new Date();
  selectedDate: Date | null = null;
  days: (Date | null)[] = [];
  months: string[] = [
    'January', 'February', 'March', 'April', 'May', 'June',
    'July', 'August', 'September', 'October', 'November', 'December'
  ];
  daysOfWeek: string[] = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];

  ngOnInit() {
    this.currentDate = this.initialDate ? new Date(this.initialDate) : new Date();
    // Set today as the initially selected date
    this.selectedDate = new Date();
    this.generateCalendar();
  }

  generateCalendar() {
    const year = this.currentDate.getFullYear();
    const month = this.currentDate.getMonth();
    const firstDay = new Date(year, month, 1);
    const lastDay = new Date(year, month + 1, 0);
    const daysInMonth = lastDay.getDate();
    const startingDay = firstDay.getDay();

    this.days = [];
    
    // Add empty slots for days before the 1st
    for (let i = 0; i < startingDay; i++) {
      this.days.push(null);
    }
    
    // Add days of the month
    for (let day = 1; day <= daysInMonth; day++) {
      this.days.push(new Date(year, month, day));
    }
  }

  prevMonth() {
    // Create a new Date object instead of mutating the existing one
    this.currentDate = new Date(this.currentDate.getFullYear(), this.currentDate.getMonth() - 1, 1);
    this.generateCalendar();
  }

  nextMonth() {
    // Create a new Date object instead of mutating the existing one
    this.currentDate = new Date(this.currentDate.getFullYear(), this.currentDate.getMonth() + 1, 1);
    this.generateCalendar();
  }

  selectDate(day: Date | null) {
    if (day && day instanceof Date && !isNaN(day.getTime())) {
      this.selectedDate = new Date(day.getTime()); // Create a proper copy
      // Format as YYYY-MM-DD in local timezone
      const year = day.getFullYear();
      const month = String(day.getMonth() + 1).padStart(2, '0');
      const dayNum = String(day.getDate()).padStart(2, '0');
      const selectedDateStr = `${year}-${month}-${dayNum}`;
      this.dateSelected.emit(selectedDateStr);
    }
  }

  // Helper method to check if a day is today
  isToday(day: Date | null): boolean {
    if (!day) return false;
    const today = new Date();
    return day.getFullYear() === today.getFullYear() &&
           day.getMonth() === today.getMonth() &&
           day.getDate() === today.getDate();
  }

  // Helper method to check if a day is selected
  isSelected(day: Date | null): boolean {
    if (!day || !this.selectedDate) return false;
    return day.getFullYear() === this.selectedDate.getFullYear() &&
           day.getMonth() === this.selectedDate.getMonth() &&
           day.getDate() === this.selectedDate.getDate();
  }

  // Helper method to check if day is valid
  isValidDay(day: Date | null): boolean {
    return day !== null && day instanceof Date && !isNaN(day.getTime());
  }

  cancel() {
    this.cancelled.emit();
  }
}