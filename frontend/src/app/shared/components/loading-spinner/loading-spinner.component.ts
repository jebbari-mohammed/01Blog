import { Component } from '@angular/core';
import { MaterialModule } from '../../material.module';

/**
 * LoadingSpinnerComponent - Simple loading indicator
 * 
 * Shows a centered spinner with "Loading..." text
 */

@Component({
  selector: 'app-loading-spinner',
  standalone: true,
  imports: [MaterialModule],
  templateUrl: './loading-spinner.component.html',
  styleUrl: './loading-spinner.component.scss'
})
export class LoadingSpinnerComponent {

}
