# Frontend Interview Questions & Answers

## 1. Component Architecture & Angular Signals

**Q: Explain how you implemented state management in your FlightSearchComponent using Angular Signals. What are the advantages of this approach?**

**A:** I used Angular Signals for reactive state management throughout the FlightSearchComponent. Key signals include:
- `searchParams` for search criteria
- `isLoading` for loading states
- `hasSearched` for view switching
- `filteredFlights` for computed flight data

Advantages:
- **Reactive Updates**: Automatic UI updates when signal values change
- **Performance**: Fine-grained reactivity without zone.js overhead
- **Simplicity**: No need for external state management libraries
- **Type Safety**: Full TypeScript support with compile-time checking

The computed `filteredFlights()` signal automatically recalculates when filters or search results change, providing efficient reactive updates.

## 2. Dual View Implementation

**Q: How did you implement the landing page and results page in a single component? What design patterns did you use?**

**A:** I implemented a dual view system using the `hasSearched` signal as a view state controller:

```typescript
hasSearched = signal(false);
```

The template uses `*ngIf="!hasSearched()"` for the landing page and `*ngIf="hasSearched()"` for results. This approach:
- **Single Component**: Maintains state across views
- **Smooth Transitions**: No route changes or component destruction
- **Shared State**: Search parameters persist between views
- **Performance**: Avoids component recreation overhead

The `goBack()` method resets the state to return to the landing page while preserving search history.

## 3. Custom Calendar Implementation

**Q: Why did you build a custom calendar component instead of using a third-party library? Walk through your implementation.**

**A:** I built a custom CalendarComponent to:
- **Control Dependencies**: Avoid external library bloat
- **Customization**: Full control over styling and behavior
- **Learning**: Demonstrate vanilla Angular/TypeScript skills

Key implementation details:
- **Date Generation**: `generateCalendar()` creates a grid of dates with proper month boundaries
- **Navigation**: `prevMonth()`/`nextMonth()` with immutable date handling
- **Selection Logic**: `selectDate()` with proper date formatting (YYYY-MM-DD)
- **Visual States**: Today highlighting, selection states, and empty cells

The component properly handles edge cases like month boundaries and leap years while maintaining a clean API with `@Input()` and `@Output()` decorators.

## 4. HTTP Service and Error Handling

**Q: Explain your FlightService implementation. How do you handle errors and data transformation?**

**A:** The FlightService implements comprehensive HTTP communication:

**Error Handling:**
```typescript
.pipe(
  catchError(error => {
    console.error('Error fetching flights:', error);
    return of({ flights: [], total: 0 });
  })
)
```

**Data Enrichment:**
I enrich backend data with UI-friendly properties:
- Random airline names for visual appeal
- Formatted flight times from date strings
- Mock data for stops and baggage information

**Type Safety:**
Strong TypeScript interfaces ensure compile-time safety:
- `Flight` interface for flight data
- `FlightSearchParams` for search criteria
- `ReservationRequest`/`ReservationResponse` for booking flow

This approach provides resilient error handling while maintaining a rich user experience.

## 5. Filter Implementation

**Q: How did you implement the real-time filtering system in FilterSidebarComponent?**

**A:** The filtering system uses:

**Signal-based State:**
```typescript
filters = signal({
  direct: true,
  oneStop: true,
  multipleStops: true
});
```

**Range Sliders:**
Custom dual-range sliders for time and duration with:
- Min/max value constraints
- Real-time updates via `(input)` events
- Proper value formatting (`formatTime()`, `formatDuration()`)

**Event Communication:**
Immediate filter emission via `EventEmitter`:
```typescript
@Output() filtersChanged = new EventEmitter<FilterOptions>();
```

**Client-side Filtering:**
The parent component applies filters in `filteredFlights()` computed signal, checking:
- Stop types (direct, 1 stop, 2+ stops)
- Departure time ranges
- Journey duration ranges

This provides instant feedback without server round-trips.

## 6. Form Validation and User Experience

**Q: Describe your approach to form validation in the reservation popup. How do you provide user feedback?**

**A:** I implemented multi-layer validation:

**Template-driven Validation:**
```html
<input
  [(ngModel)]="reservationRequest.passager.nom"
  required
  #nomField="ngModel"
/>
```

**Real-time Feedback:**
- Form validity checking with `reservationForm.form.valid`
- Disabled submit button for invalid forms
- Error message display for API errors

**User Experience:**
- **Loading States**: Button text changes during submission
- **Error Handling**: Specific error messages from backend
- **Success Flow**: Confirmation popup with reservation details
- **Form Reset**: Clean state when reopening popup

The validation provides immediate feedback while preventing invalid submissions.

## 7. Responsive Design Implementation

**Q: How did you ensure your application works across different screen sizes?**

**A:** I implemented responsive design using:

**Tailwind CSS Utilities:**
- `grid-cols-1 lg:grid-cols-12` for responsive grid layouts
- `flex-col lg:flex-row` for adaptive flex directions
- `hidden lg:block` for conditional element display

**Mobile-first Approach:**
- Base styles for mobile devices
- Progressive enhancement for larger screens
- Touch-friendly button sizes and spacing

**Component Adaptability:**
- FlightReservationCardComponent adapts layout for mobile
- Calendar component maintains usability on small screens
- Filter sidebar collapses appropriately

**CSS Grid/Flexbox:**
Strategic use of modern CSS layout methods for flexible, responsive designs without media queries.

## 8. Event Handling and Component Communication

**Q: Explain how components communicate in your application. What patterns did you use?**

**A:** I used several communication patterns:

**Parent-Child Communication:**
- `@Input()` for data passing (flight data to cards)
- `@Output()` with `EventEmitter` for child-to-parent events

**Event Bubbling:**
```typescript
@Output() reserve = new EventEmitter<void>();
```

**Service-based Communication:**
- FlightService for HTTP operations
- Shared state through service injection

**Signal-based Updates:**
- Reactive updates through signal changes
- Computed signals for derived state

**Event Propagation Control:**
```typescript
stopPropagation(event: Event) {
  event.stopPropagation();
}
```

This approach maintains clean separation of concerns while enabling efficient data flow.

## 9. Performance Optimization Strategies

**Q: What performance optimizations did you implement in your frontend application?**

**A:** Several optimization strategies:

**Signal-based Reactivity:**
- Fine-grained updates without zone.js overhead
- Computed signals prevent unnecessary recalculations

**Efficient Change Detection:**
- Standalone components reduce bundle size
- OnPush change detection strategy considerations

**Client-side Filtering:**
- Immediate filter results without server requests
- Cached filter computations

**Lazy Loading Potential:**
- Standalone components enable easy code splitting
- Component-level optimization opportunities

**Memory Management:**
- Proper subscription handling in services
- Event listener cleanup in components

**Bundle Optimization:**
- Tree-shaking friendly imports
- Minimal external dependencies

## 10. TypeScript and Code Quality

**Q: How did you ensure type safety and code quality throughout your frontend application?**

**A:** I implemented comprehensive TypeScript practices:

**Strong Typing:**
```typescript
interface Flight {
  id: string;
  villeDepart: string;
  villeArrivee: string;
  // ... other properties
}
```

**Type Guards:**
- Null checking with optional chaining
- Proper type assertions where necessary
- Runtime type validation for API responses

**Code Organization:**
- Clear interface definitions for all data structures
- Consistent naming conventions
- Proper separation of concerns

**Error Handling:**
- Typed error responses
- Comprehensive try-catch blocks
- User-friendly error messages

**Development Tools:**
- Strict TypeScript configuration
- ESLint integration potential
- Consistent code formatting

This approach ensures maintainable, bug-free code with excellent developer experience.