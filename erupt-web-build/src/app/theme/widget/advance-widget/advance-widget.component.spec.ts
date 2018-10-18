import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AdvanceWidgetComponent } from './advance-widget.component';

describe('AdvanceWidgetComponent', () => {
  let component: AdvanceWidgetComponent;
  let fixture: ComponentFixture<AdvanceWidgetComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AdvanceWidgetComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AdvanceWidgetComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
