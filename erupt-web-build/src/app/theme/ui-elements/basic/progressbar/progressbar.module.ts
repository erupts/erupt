import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ProgressbarComponent } from './progressbar.component';
import {ProgressbarRoutingModule} from './progressbar-routing.module';
import {SharedModule} from '../../../../shared/shared.module';

@NgModule({
  imports: [
    CommonModule,
    ProgressbarRoutingModule,
    SharedModule
  ],
  declarations: [ProgressbarComponent]
})
export class ProgressbarModule { }
