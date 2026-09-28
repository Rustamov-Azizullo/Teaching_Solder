import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { authLabels } from '../labels';
import { LoginForm } from './LoginForm';

const login = vi.fn();

vi.mock('@/features/auth/hooks/useAuth', () => ({
  useAuth: () => ({ login }),
}));

vi.mock('@/lib/apiClient', () => ({
  getErrorMessage: (error: unknown) => (error instanceof Error ? error.message : 'unknown'),
}));

const fillAndSubmit = async (username: string, password: string) => {
  await userEvent.type(screen.getByLabelText(authLabels.username), username);
  await userEvent.type(screen.getByLabelText(authLabels.password), password);
  await userEvent.click(screen.getByRole('button', { name: authLabels.submit }));
};

describe('LoginForm', () => {
  beforeEach(() => {
    login.mockReset();
  });

  it('calls login with the entered username and password', async () => {
    login.mockResolvedValue(undefined);
    render(<LoginForm />);

    await fillAndSubmit('hktb', 'Parol123!');

    await waitFor(() => expect(login).toHaveBeenCalledWith('hktb', 'Parol123!', undefined));
  });

  it('shows the server error message when login rejects', async () => {
    login.mockRejectedValue(new Error('Login yoki parol xato'));
    render(<LoginForm />);

    await fillAndSubmit('hktb', 'wrong');

    expect(await screen.findByText('Login yoki parol xato')).toBeInTheDocument();
  });

  it('does not call login and shows validation messages when fields are empty', async () => {
    render(<LoginForm />);

    await userEvent.click(screen.getByRole('button', { name: authLabels.submit }));

    expect(await screen.findByText(authLabels.usernameRequired)).toBeInTheDocument();
    expect(screen.getByText(authLabels.passwordRequired)).toBeInTheDocument();
    expect(login).not.toHaveBeenCalled();
  });
});
