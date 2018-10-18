import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CarouselComponent } from './carousel.component';
import {CarouselRoutingModule} from './carousel-routing.module';
import {SharedModule} from '../../../../shared/shared.module';
import {NgxCarouselModule} from 'ngx-carousel';
import 'hammerjs';

@NgModule({
  imports: [
    CommonModule,
    CarouselRoutingModule,
    SharedModule,
    NgxCarouselModule
  ],
  declarations: [CarouselComponent]
})
export class CarouselModule { }
