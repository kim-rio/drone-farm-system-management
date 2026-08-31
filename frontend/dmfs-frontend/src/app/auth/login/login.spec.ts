import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { Login } from './login';
import { AuthService } from '../../services/auth.service';

describe('Login', () => {
  let component: Login;
  let fixture: ComponentFixture<Login>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Login],
      providers: [
        {
          provide: AuthService,
          useValue: {
            login: jasmine.createSpy('login'),
            logout: jasmine.createSpy('logout'),
            isLoggedIn: jasmine.createSpy('isLoggedIn')
          }
        },
        provideRouter([])
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(Login);
    component = fixture.componentInstance;

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
