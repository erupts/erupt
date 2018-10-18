import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MultiRowsComponent } from './multi-rows.component';

describe('MultiRowsComponent', () => {
  let component: MultiRowsComponent;
  let fixture: ComponentFixture<MultiRowsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MultiRowsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MultiRowsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
