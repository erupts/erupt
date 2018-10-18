import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RatingComponent } from './rating.component';
import {RatingRoutingModule} from './rating-routing.module';
import {SharedModule} from '../../../../shared/shared.module';
import {BarRatingModule} from 'ngx-bar-rating';

@NgModule({
  imports: [
    CommonModule,
    RatingRoutingModule,
    SharedModule,
    BarRatingModule
  ],
  declarations: [RatingComponent]
})
export class RatingModule { }
