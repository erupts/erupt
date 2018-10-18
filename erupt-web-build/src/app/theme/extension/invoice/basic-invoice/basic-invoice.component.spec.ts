import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { BasicInvoiceComponent } from './basic-invoice.component';

describe('BasicInvoiceComponent', () => {
  let component: BasicInvoiceComponent;
  let fixture: ComponentFixture<BasicInvoiceComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ BasicInvoiceComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(BasicInvoiceComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
